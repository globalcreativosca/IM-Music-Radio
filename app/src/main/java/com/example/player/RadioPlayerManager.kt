package com.example.player

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.CountDownTimer
import android.os.PowerManager
import com.example.model.EqualizerPreset
import com.example.model.PlaybackState
import com.example.model.RadioStation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

class RadioPlayerManager(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var mediaPlayer: MediaPlayer? = null

    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _currentStation = MutableStateFlow<RadioStation?>(null)
    val currentStation: StateFlow<RadioStation?> = _currentStation.asStateFlow()

    private val _volume = MutableStateFlow(0.85f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _equalizerPreset = MutableStateFlow(EqualizerPreset.NORMAL)
    val equalizerPreset: StateFlow<EqualizerPreset> = _equalizerPreset.asStateFlow()

    private val _sleepTimerSeconds = MutableStateFlow(0)
    val sleepTimerSeconds: StateFlow<Int> = _sleepTimerSeconds.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // 16 spectrum bar levels for UI visualizer
    private val _visualizerBars = MutableStateFlow(List(16) { 0.15f })
    val visualizerBars: StateFlow<List<Float>> = _visualizerBars.asStateFlow()

    private var sleepCountDownTimer: CountDownTimer? = null
    private val playerScope = CoroutineScope(Dispatchers.Main + Job())
    private var visualizerJob: Job? = null

    private var focusRequest: AudioFocusRequest? = null

    init {
        startVisualizerLoop()
    }

    private fun startVisualizerLoop() {
        visualizerJob?.cancel()
        visualizerJob = playerScope.launch {
            var step = 0f
            while (isActive) {
                if (_playbackState.value == PlaybackState.PLAYING && !_isMuted.value && _volume.value > 0.02f) {
                    val volMultiplier = _volume.value
                    val preset = _equalizerPreset.value
                    val bars = List(16) { index ->
                        val base = (sin(step + index * 0.45f) * 0.35f + 0.45f)
                        val noise = Random.nextFloat() * 0.25f
                        val eqFactor = if (index < 5) preset.bassFactor else if (index > 11) preset.trebleFactor else 1.0f
                        ((base + noise) * volMultiplier * eqFactor).coerceIn(0.08f, 1.0f)
                    }
                    _visualizerBars.value = bars
                    step += 0.22f
                } else if (_playbackState.value == PlaybackState.BUFFERING) {
                    // Pulsing wave while buffering
                    val bars = List(16) { index ->
                        (sin(step + index * 0.5f) * 0.3f + 0.4f).coerceIn(0.1f, 0.7f)
                    }
                    _visualizerBars.value = bars
                    step += 0.35f
                } else {
                    _visualizerBars.value = List(16) { 0.1f }
                }
                delay(70)
            }
        }
    }

    fun play(station: RadioStation) {
        _errorMessage.value = null
        _currentStation.value = station
        _playbackState.value = PlaybackState.BUFFERING

        requestAudioFocus()

        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                reset()
                release()
            }
            mediaPlayer = null

            val player = MediaPlayer().apply {
                setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(station.streamUrl)
                setOnPreparedListener { mp ->
                    applyVolume(mp)
                    mp.start()
                    _playbackState.value = PlaybackState.PLAYING
                }
                setOnBufferingUpdateListener { _, percent ->
                    if (percent in 1..99 && _playbackState.value != PlaybackState.PLAYING) {
                        _playbackState.value = PlaybackState.BUFFERING
                    }
                }
                setOnErrorListener { _, what, extra ->
                    _playbackState.value = PlaybackState.ERROR
                    _errorMessage.value = "Error al conectar con la transmisión ($what, $extra)"
                    true
                }
                prepareAsync()
            }
            mediaPlayer = player
        } catch (e: Exception) {
            _playbackState.value = PlaybackState.ERROR
            _errorMessage.value = "No se pudo iniciar el reproductor: ${e.localizedMessage}"
        }
    }

    fun resume() {
        val st = _currentStation.value
        if (mediaPlayer != null) {
            try {
                mediaPlayer?.start()
                _playbackState.value = PlaybackState.PLAYING
            } catch (e: Exception) {
                if (st != null) play(st)
            }
        } else if (st != null) {
            play(st)
        }
    }

    fun pause() {
        try {
            mediaPlayer?.pause()
            _playbackState.value = PlaybackState.PAUSED
        } catch (e: Exception) {
            stop()
        }
    }

    fun stop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.reset()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        _playbackState.value = PlaybackState.IDLE
        abandonAudioFocus()
    }

    fun togglePlayPause() {
        when (_playbackState.value) {
            PlaybackState.PLAYING -> pause()
            PlaybackState.PAUSED -> resume()
            PlaybackState.IDLE, PlaybackState.ERROR -> {
                _currentStation.value?.let { play(it) }
            }
            PlaybackState.BUFFERING -> stop()
        }
    }

    fun setVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        _volume.value = clamped
        if (clamped > 0f && _isMuted.value) {
            _isMuted.value = false
        }
        mediaPlayer?.let { applyVolume(it) }
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
        mediaPlayer?.let { applyVolume(it) }
    }

    private fun applyVolume(mp: MediaPlayer) {
        val target = if (_isMuted.value) 0f else _volume.value
        val eqPreset = _equalizerPreset.value
        val gain = (target * ((eqPreset.bassFactor + eqPreset.trebleFactor) / 2f)).coerceIn(0f, 1f)
        try {
            mp.setVolume(gain, gain)
        } catch (_: Exception) {}
    }

    fun setEqualizerPreset(preset: EqualizerPreset) {
        _equalizerPreset.value = preset
        mediaPlayer?.let { applyVolume(it) }
    }

    fun setSleepTimer(minutes: Int) {
        sleepCountDownTimer?.cancel()
        sleepCountDownTimer = null

        if (minutes <= 0) {
            _sleepTimerSeconds.value = 0
            return
        }

        val totalMillis = minutes * 60 * 1000L
        _sleepTimerSeconds.value = minutes * 60

        sleepCountDownTimer = object : CountDownTimer(totalMillis, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                _sleepTimerSeconds.value = (millisUntilFinished / 1000L).toInt()
            }

            override fun onFinish() {
                _sleepTimerSeconds.value = 0
                pause()
            }
        }.start()
    }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                .setOnAudioFocusChangeListener { focusChange ->
                    when (focusChange) {
                        AudioManager.AUDIOFOCUS_LOSS -> pause()
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pause()
                        AudioManager.AUDIOFOCUS_GAIN -> resume()
                    }
                }
                .build()
            focusRequest = req
            audioManager.requestAudioFocus(req)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
    }

    fun release() {
        stop()
        visualizerJob?.cancel()
        sleepCountDownTimer?.cancel()
    }
}
