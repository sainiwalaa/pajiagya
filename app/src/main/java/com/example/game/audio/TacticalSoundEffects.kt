package com.example.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

class TacticalSoundEffects(private val context: Context) {

    private val vibrator: Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    } catch (_: Exception) {
        null
    }

    private val scope = CoroutineScope(Dispatchers.Default)

    fun playGunshot() {
        scope.launch {
            vibrate(35, 180)
            generateGunshotAudio()
        }
    }

    fun playHeavyShot() {
        scope.launch {
            vibrate(50, 255)
            generateAudioTone(120, 80)
        }
    }

    fun playHitMarker() {
        scope.launch {
            vibrate(20, 100)
            generateAudioTone(880, 40)
        }
    }

    fun playEnemyEliminated() {
        scope.launch {
            vibratePattern(longArrayOf(0, 40, 50, 60))
            generateAudioTone(587, 80)
            generateAudioTone(880, 100)
        }
    }

    fun playExplosion() {
        scope.launch {
            vibratePattern(longArrayOf(0, 100, 50, 150))
            generateAudioTone(60, 280)
        }
    }

    fun playReload() {
        scope.launch {
            vibrate(25, 80)
            generateAudioTone(440, 50)
        }
    }

    fun playItemPickup() {
        scope.launch {
            vibrate(15, 60)
            generateAudioTone(659, 50)
        }
    }

    fun playVehicleEngine() {
        scope.launch {
            vibrate(30, 70)
            generateAudioTone(95, 70)
        }
    }

    fun playRecallBeacon() {
        scope.launch {
            vibratePattern(longArrayOf(0, 60, 40, 60, 40, 100))
            generateAudioTone(523, 100)
            generateAudioTone(659, 100)
            generateAudioTone(784, 150)
        }
    }

    private fun vibrate(durationMs: Long, amplitude: Int = 120) {
        try {
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(
                            durationMs,
                            amplitude.coerceIn(1, 255)
                        )
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(durationMs)
                }
            }
        } catch (_: Exception) {}
    }

    private fun vibratePattern(timings: LongArray) {
        try {
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(timings, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(timings, -1)
                }
            }
        } catch (_: Exception) {}
    }

    private fun generateGunshotAudio() {
        try {
            val sampleRate = 22050
            val durationMs = 60
            val numSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(numSamples)
            var currentFreq = 300.0

            for (i in 0 until numSamples) {
                val decay = 1.0 - (i.toDouble() / numSamples)
                val noise = (Math.random() * 2.0 - 1.0) * 0.4
                val tone = sin(2.0 * Math.PI * i / (sampleRate / currentFreq)) * 0.6
                buffer[i] = (((noise + tone) * decay * 32767.0) * 0.6).toInt().toShort()
                currentFreq = (currentFreq * 0.98).coerceAtLeast(80.0)
            }

            playPcmBuffer(buffer, sampleRate)
        } catch (_: Exception) {}
    }

    private fun generateAudioTone(freqHz: Int, durationMs: Int) {
        try {
            val sampleRate = 22050
            val numSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val angle = 2.0 * Math.PI * i / (sampleRate.toDouble() / freqHz)
                val decay = 1.0 - (i.toDouble() / numSamples)
                buffer[i] = (sin(angle) * decay * 32767.0 * 0.4).toInt().toShort()
            }

            playPcmBuffer(buffer, sampleRate)
        } catch (_: Exception) {}
    }

    private fun playPcmBuffer(buffer: ShortArray, sampleRate: Int) {
        try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buffer, 0, buffer.size)
            track.play()
            // Track will release after playing
            scope.launch {
                kotlinx.coroutines.delay(buffer.size * 1000L / sampleRate + 50L)
                try {
                    track.stop()
                    track.release()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
    }
}
