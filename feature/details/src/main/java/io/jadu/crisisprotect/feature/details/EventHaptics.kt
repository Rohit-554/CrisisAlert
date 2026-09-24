package io.jadu.crisisprotect.feature.details

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import io.jadu.crisisprotect.domain.model.DisasterEvent
import io.jadu.crisisprotect.domain.model.DisasterType

sealed interface EventHapticAvailability {
    data object Supported : EventHapticAvailability
    data object UnsupportedEvent : EventHapticAvailability
    data object Unavailable : EventHapticAvailability
}

interface EventHapticController {
    fun availabilityFor(event: DisasterEvent): EventHapticAvailability
    fun play(event: DisasterEvent): Long?
    fun stop()
}

class AndroidEventHapticController(context: Context) : EventHapticController {
    private val appContext = context.applicationContext
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        appContext.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    override fun availabilityFor(event: DisasterEvent): EventHapticAvailability = when {
        patternFor(event) == null -> EventHapticAvailability.UnsupportedEvent
        vibrator?.hasVibrator() != true || !isHapticFeedbackEnabled() -> EventHapticAvailability.Unavailable
        else -> EventHapticAvailability.Supported
    }

    override fun play(event: DisasterEvent): Long? {
        val pattern = patternFor(event) ?: return null
        if (availabilityFor(event) != EventHapticAvailability.Supported) return null
        val vibrator = vibrator ?: return null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = if (vibrator.hasAmplitudeControl()) {
                VibrationEffect.createWaveform(pattern.timings, pattern.amplitudes, -1)
            } else {
                VibrationEffect.createWaveform(pattern.timings, -1)
            }
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern.timings, -1)
        }
        return pattern.durationMillis
    }

    override fun stop() {
        vibrator?.cancel()
    }

    private fun isHapticFeedbackEnabled(): Boolean = Settings.System.getInt(
        appContext.contentResolver,
        Settings.System.HAPTIC_FEEDBACK_ENABLED,
        1,
    ) != 0

    private fun patternFor(event: DisasterEvent): HapticPattern? = when (event.type) {
        DisasterType.EARTHQUAKE -> when (val magnitude = event.magnitude) {
            null -> HapticPattern.MediumEarthquake
            in Double.NEGATIVE_INFINITY..<4.0 -> HapticPattern.LightEarthquake
            in 4.0..<6.0 -> HapticPattern.MediumEarthquake
            else -> HapticPattern.StrongEarthquake
        }
        DisasterType.WILDFIRE -> HapticPattern.Wildfire
        DisasterType.STORM -> HapticPattern.Storm
        DisasterType.FLOOD -> HapticPattern.Flood
        DisasterType.VOLCANO -> HapticPattern.Volcano
        DisasterType.OTHER -> null
    }
}

private data class HapticPattern(val timings: LongArray, val amplitudes: IntArray) {
    val durationMillis: Long = timings.sum()

    companion object {
        val LightEarthquake = HapticPattern(longArrayOf(0, 45, 130, 45, 130, 45), intArrayOf(0, 110, 0, 125, 0, 110))
        val MediumEarthquake = HapticPattern(longArrayOf(0, 65, 95, 75, 95, 85, 95, 75), intArrayOf(0, 135, 0, 165, 0, 180, 0, 150))
        val StrongEarthquake = HapticPattern(longArrayOf(0, 85, 65, 120, 65, 150, 65, 180), intArrayOf(0, 160, 0, 205, 0, 240, 0, 220))
        val Wildfire = HapticPattern(longArrayOf(0, 55, 120, 85, 120, 115, 120, 145), intArrayOf(0, 90, 0, 120, 0, 155, 0, 185))
        val Storm = HapticPattern(longArrayOf(0, 90, 65, 45, 160, 110, 70, 55), intArrayOf(0, 175, 0, 105, 0, 205, 0, 145))
        val Flood = HapticPattern(longArrayOf(0, 150, 110, 170, 110, 150), intArrayOf(0, 120, 0, 145, 0, 125))
        val Volcano = HapticPattern(longArrayOf(0, 70, 100, 95, 85, 150, 70, 175), intArrayOf(0, 110, 0, 155, 0, 215, 0, 245))
    }
}
