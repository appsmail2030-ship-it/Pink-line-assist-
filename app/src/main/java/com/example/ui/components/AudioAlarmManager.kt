package com.example.ui.components

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.service.AlertMediaPlayerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * Robust Audio and Siren ("Ciren") Sound Synthesizer for Pink Line Assist.
 *
 * Uses direct AudioTrack PCM synthesis to generate authentic wailing sirens,
 * 2-3 station advance approach sirens, critical 1-station alarms, destination alarms,
 * and acknowledgment chimes, with ToneGenerator and RingtoneManager fallbacks.
 */
class AudioAlarmManager(private val context: Context) {

    private var toneGenerator: ToneGenerator? = null
    private var activeAudioTrack: AudioTrack? = null
    private var alarmJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
        } catch (_: Exception) {
            try {
                toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 90)
            } catch (_: Exception) {}
        }
    }

    /**
     * Authentic Wailing Metro Emergency Siren ("Ciren").
     * Sweeps smoothly between 600 Hz and 1350 Hz in a repeating wave.
     */
    fun playSirenAlarm(durationMs: Long = 4000) {
        stopAlarm()
        try {
            AlertMediaPlayerService.startAlert(
                context = context,
                alertType = "DESTINATION_ASSISTANCE",
                title = "Emergency Siren Activated",
                message = "High-priority assistance siren sounding."
            )
        } catch (_: Exception) {}
        alarmJob = scope.launch {
            val vibrator = getVibrator()
            val sampleRate = 44100
            val minBufSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(4410)

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val audioFormat = AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .build()

            var track: AudioTrack? = null
            try {
                track = AudioTrack.Builder()
                    .setAudioAttributes(audioAttributes)
                    .setAudioFormat(audioFormat)
                    .setBufferSizeInBytes(minBufSize * 2)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
                activeAudioTrack = track
                track.play()

                val startTime = System.currentTimeMillis()
                var phase = 0.0
                val sweepDurationSec = 0.8 // Cycle time for up-down sweep
                val chunkSamples = 1024
                val buffer = ShortArray(chunkSamples)

                while (isActive && (System.currentTimeMillis() - startTime < durationMs)) {
                    val elapsedSec = (System.currentTimeMillis() - startTime) / 1000.0
                    // Triangle or sine sweep between 650Hz and 1300Hz
                    val sweepPhase = (elapsedSec % sweepDurationSec) / sweepDurationSec
                    val freq = if (sweepPhase < 0.5) {
                        650.0 + (1300.0 - 650.0) * (sweepPhase * 2.0)
                    } else {
                        1300.0 - (1300.0 - 650.0) * ((sweepPhase - 0.5) * 2.0)
                    }

                    for (i in 0 until chunkSamples) {
                        val sample = (sin(phase) * 30000.0).toInt().toShort()
                        buffer[i] = sample
                        phase += 2.0 * PI * freq / sampleRate
                        if (phase > 2.0 * PI) phase -= 2.0 * PI
                    }

                    track.write(buffer, 0, chunkSamples)
                    vibratePattern(vibrator, longArrayOf(0, 180, 80, 180))
                }
            } catch (_: Exception) {
                // Fallback to ToneGenerator / Ringtone
                playFallbackTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, durationMs)
            } finally {
                try {
                    track?.stop()
                    track?.release()
                } catch (_: Exception) {}
            }
        }
    }

    /**
     * 2-3 Station Advance Warning Alarm Siren.
     * Generates a pulsed alert chime and rising pitch sweep (750 Hz -> 1100 Hz).
     */
    fun playWarningApproachAlarm(
        durationMs: Long = 3500,
        stationName: String = "",
        trainId: String = "",
        platform: String = ""
    ) {
        stopAlarm()
        try {
            AlertMediaPlayerService.startAlert(
                context = context,
                alertType = "2-3_STATION_WARNING",
                title = if (stationName.isNotEmpty()) "⚠️ Approaching $stationName" else "⚠️ 2-3 Stations Advance Alert",
                message = "Train ${if (trainId.isNotEmpty()) trainId else "metro"} is 2-3 stations away. Prepare assistance.",
                stationName = stationName,
                trainId = trainId,
                circularPlatform = platform
            )
        } catch (_: Exception) {}
        alarmJob = scope.launch {
            val vibrator = getVibrator()
            val sampleRate = 44100
            val chunkSamples = 1024
            val buffer = ShortArray(chunkSamples)

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val audioFormat = AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .build()

            var track: AudioTrack? = null
            try {
                track = AudioTrack.Builder()
                    .setAudioAttributes(audioAttributes)
                    .setAudioFormat(audioFormat)
                    .setBufferSizeInBytes(chunkSamples * 4)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
                activeAudioTrack = track
                track.play()

                val startTime = System.currentTimeMillis()
                var phase = 0.0

                while (isActive && (System.currentTimeMillis() - startTime < durationMs)) {
                    val cycleTime = ((System.currentTimeMillis() - startTime) % 500) / 500.0
                    val isPulseOn = cycleTime < 0.7
                    val freq = 750.0 + (350.0 * cycleTime)

                    for (i in 0 until chunkSamples) {
                        val amp = if (isPulseOn) 28000.0 else 0.0
                        buffer[i] = (sin(phase) * amp).toInt().toShort()
                        phase += 2.0 * PI * freq / sampleRate
                        if (phase > 2.0 * PI) phase -= 2.0 * PI
                    }

                    track.write(buffer, 0, chunkSamples)
                    vibratePattern(vibrator, longArrayOf(0, 120, 80, 120))
                }
            } catch (_: Exception) {
                playFallbackTone(ToneGenerator.TONE_CDMA_HIGH_L, durationMs)
            } finally {
                try {
                    track?.stop()
                    track?.release()
                } catch (_: Exception) {}
            }
        }
    }

    /**
     * 1-Station Critical Approach Rapid Hi-Lo Siren (900 Hz / 1400 Hz).
     */
    fun playCriticalSiren(
        durationMs: Long = 4000,
        stationName: String = "",
        trainId: String = "",
        platform: String = ""
    ) {
        stopAlarm()
        try {
            AlertMediaPlayerService.startAlert(
                context = context,
                alertType = "1_STATION_CRITICAL",
                title = if (stationName.isNotEmpty()) "⚡ Final Station Approach: $stationName" else "⚡ 1 Station Critical Alert",
                message = "Train ${if (trainId.isNotEmpty()) trainId else "metro"} is at the immediate previous station! Stand by on $platform.",
                stationName = stationName,
                trainId = trainId,
                circularPlatform = platform
            )
        } catch (_: Exception) {}
        alarmJob = scope.launch {
            val vibrator = getVibrator()
            val sampleRate = 44100
            val chunkSamples = 1024
            val buffer = ShortArray(chunkSamples)

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val audioFormat = AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .build()

            var track: AudioTrack? = null
            try {
                track = AudioTrack.Builder()
                    .setAudioAttributes(audioAttributes)
                    .setAudioFormat(audioFormat)
                    .setBufferSizeInBytes(chunkSamples * 4)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
                activeAudioTrack = track
                track.play()

                val startTime = System.currentTimeMillis()
                var phase = 0.0

                while (isActive && (System.currentTimeMillis() - startTime < durationMs)) {
                    val step = (((System.currentTimeMillis() - startTime) / 140) % 2).toInt()
                    val freq = if (step == 0) 900.0 else 1400.0

                    for (i in 0 until chunkSamples) {
                        buffer[i] = (sin(phase) * 31000.0).toInt().toShort()
                        phase += 2.0 * PI * freq / sampleRate
                        if (phase > 2.0 * PI) phase -= 2.0 * PI
                    }

                    track.write(buffer, 0, chunkSamples)
                    vibratePattern(vibrator, longArrayOf(0, 140, 60, 140))
                }
            } catch (_: Exception) {
                playFallbackTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, durationMs)
            } finally {
                try {
                    track?.stop()
                    track?.release()
                } catch (_: Exception) {}
            }
        }
    }

    /**
     * Destination Arrival Siren & Chime.
     */
    fun playDestinationAlarm(
        durationMs: Long = 5000,
        stationName: String = "",
        trainId: String = "",
        platform: String = ""
    ) {
        stopAlarm()
        try {
            AlertMediaPlayerService.startAlert(
                context = context,
                alertType = "DESTINATION_ASSISTANCE",
                title = if (stationName.isNotEmpty()) "🚨 Train Arrived at $stationName" else "🚨 Destination Arrival Alert",
                message = "Train ${if (trainId.isNotEmpty()) trainId else "metro"} has ARRIVED. Assist Divyangjan passenger immediately at $platform.",
                stationName = stationName,
                trainId = trainId,
                circularPlatform = platform
            )
        } catch (_: Exception) {}
        playSirenAlarm(durationMs)
    }

    /**
     * Priority Alarm (Used for general priority alerts).
     */
    fun playPriorityAlarm(durationMs: Long = 3500) {
        playWarningApproachAlarm(durationMs)
    }

    /**
     * Request Acknowledgment Chime.
     * Plays a crisp double affirmation tone (880 Hz -> 1320 Hz) providing instant audio feedback
     * when a Station Assistant or Control Room Operator acknowledges an alert or accepts a request.
     */
    fun playAcknowledgmentChime() {
        stopAlarm()
        scope.launch {
            val vibrator = getVibrator()
            val sampleRate = 44100
            val frequencies = doubleArrayOf(880.0, 1320.0)
            val durationPerToneMs = 140

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val audioFormat = AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .build()

            var track: AudioTrack? = null
            try {
                val totalSamples = ((durationPerToneMs * 2 * sampleRate) / 1000)
                val buffer = ShortArray(totalSamples)
                var index = 0

                for (freq in frequencies) {
                    var phase = 0.0
                    val numSamples = (durationPerToneMs * sampleRate) / 1000
                    for (i in 0 until numSamples) {
                        val decay = 1.0 - (i.toDouble() / numSamples.toDouble()) * 0.4
                        val sample = (sin(phase) * 28000.0 * decay).toInt().toShort()
                        buffer[index++] = sample
                        phase += 2.0 * PI * freq / sampleRate
                        if (phase > 2.0 * PI) phase -= 2.0 * PI
                    }
                }

                track = AudioTrack.Builder()
                    .setAudioAttributes(audioAttributes)
                    .setAudioFormat(audioFormat)
                    .setBufferSizeInBytes(totalSamples * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()
                activeAudioTrack = track

                track.write(buffer, 0, totalSamples)
                track.play()
                vibratePattern(vibrator, longArrayOf(0, 80, 50, 120))
                delay(350)
            } catch (_: Exception) {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 200)
                } catch (_: Exception) {}
            } finally {
                try {
                    track?.stop()
                    track?.release()
                } catch (_: Exception) {}
            }
        }
    }

    /**
     * New Assistance Request Created Chime (Ascending 3-tone metro intake chime: 587Hz -> 880Hz -> 1174Hz).
     */
    fun playRequestCreatedChime() {
        stopAlarm()
        scope.launch {
            val vibrator = getVibrator()
            val sampleRate = 44100
            val frequencies = doubleArrayOf(587.33, 880.0, 1174.66) // D5, A5, D6
            val durationPerToneMs = 120

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val audioFormat = AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .build()

            var track: AudioTrack? = null
            try {
                val totalSamples = (durationPerToneMs * frequencies.size * sampleRate) / 1000
                val buffer = ShortArray(totalSamples)
                var index = 0

                for (freq in frequencies) {
                    var phase = 0.0
                    val numSamples = (durationPerToneMs * sampleRate) / 1000
                    for (i in 0 until numSamples) {
                        val decay = 1.0 - (i.toDouble() / numSamples.toDouble()) * 0.3
                        val sample = (sin(phase) * 26000.0 * decay).toInt().toShort()
                        buffer[index++] = sample
                        phase += 2.0 * PI * freq / sampleRate
                        if (phase > 2.0 * PI) phase -= 2.0 * PI
                    }
                }

                track = AudioTrack.Builder()
                    .setAudioAttributes(audioAttributes)
                    .setAudioFormat(audioFormat)
                    .setBufferSizeInBytes(totalSamples * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()
                activeAudioTrack = track

                track.write(buffer, 0, totalSamples)
                track.play()
                vibratePattern(vibrator, longArrayOf(0, 60, 40, 60, 40, 100))
                delay(400)
            } catch (_: Exception) {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 250)
                } catch (_: Exception) {}
            } finally {
                try {
                    track?.stop()
                    track?.release()
                } catch (_: Exception) {}
            }
        }
    }

    /**
     * Immediately stops any currently playing audio siren or tone.
     */
    fun stopAlarm() {
        try {
            AlertMediaPlayerService.stopAlert(context)
        } catch (_: Exception) {}
        alarmJob?.cancel()
        alarmJob = null
        try {
            activeAudioTrack?.pause()
            activeAudioTrack?.flush()
            activeAudioTrack?.stop()
            activeAudioTrack?.release()
            activeAudioTrack = null
        } catch (_: Exception) {}
        try {
            toneGenerator?.stopTone()
        } catch (_: Exception) {}
    }

    private fun playFallbackTone(toneType: Int, durationMs: Long) {
        scope.launch {
            try {
                toneGenerator?.startTone(toneType, durationMs.toInt().coerceAtMost(3000))
            } catch (_: Exception) {
                try {
                    val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    val ringtone = RingtoneManager.getRingtone(context, notificationUri)
                    ringtone?.play()
                } catch (_: Exception) {}
            }
        }
    }

    private fun getVibrator(): Vibrator? {
        return try {
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
    }

    private fun vibratePattern(vibrator: Vibrator?, pattern: LongArray) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, -1)
            }
        } catch (_: Exception) {}
    }
}
