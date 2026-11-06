package com.example

import com.example.model.EqualizerPreset
import com.example.model.PlaybackState
import com.example.model.RadioConfig
import com.example.model.RadioStation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun radioConfig_defaultValuesAreValid() {
        val config = RadioConfig()
        assertEquals("Radio Mix Digital", config.stationName)
        assertEquals("admin123", config.adminPassword)
        assertTrue(config.primaryStreamUrl.startsWith("http"))
        assertTrue(config.chatEnabled)
    }

    @Test
    fun equalizerPresets_haveValidGains() {
        EqualizerPreset.values().forEach { preset ->
            assertNotNull(preset.displayName)
            assertTrue(preset.bassFactor in 0.5f..2.0f)
            assertTrue(preset.trebleFactor in 0.5f..2.0f)
        }
    }

    @Test
    fun playbackState_containsExpectedStates() {
        assertEquals(PlaybackState.PLAYING, PlaybackState.valueOf("PLAYING"))
        assertEquals(PlaybackState.BUFFERING, PlaybackState.valueOf("BUFFERING"))
        assertEquals(PlaybackState.PAUSED, PlaybackState.valueOf("PAUSED"))
    }

    @Test
    fun radioStation_properties() {
        val station = RadioStation(
            id = "test_1",
            name = "Test Radio",
            genre = "Rock",
            streamUrl = "https://example.com/stream.mp3",
            bitrate = "128 kbps"
        )
        assertEquals("test_1", station.id)
        assertFalse(station.isFavorite)
    }
}
