package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.sin

/**
 * Android Foreground Service managing MediaPlayer playback and heads-up audible siren notifications
 * when high-priority assistance alerts (2-3 stations away, 1 station critical, or destination arrival)
 * are triggered on the Delhi Metro Pink Line.
 */
class AlertMediaPlayerService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var vibrator: Vibrator? = null
    private var isPlaying = false
    private val serviceScope = CoroutineScope(Dispatchers.Default)
    private var autoTimeoutJob: Job? = null

    companion object {
        const val CHANNEL_ID = "pink_line_siren_alerts"
        const val CHANNEL_NAME = "High Priority Assistance Sirens"
        const val NOTIFICATION_ID = 7001

        const val ACTION_PLAY_ALERT = "com.example.service.ACTION_PLAY_ALERT"
        const val ACTION_STOP_ALERT = "com.example.service.ACTION_STOP_ALERT"
        const val ACTION_TEST_SIREN = "com.example.service.ACTION_TEST_SIREN"

        const val EXTRA_ALERT_TYPE = "EXTRA_ALERT_TYPE"
        const val EXTRA_TITLE = "EXTRA_TITLE"
        const val EXTRA_MESSAGE = "EXTRA_MESSAGE"
        const val EXTRA_STATION_NAME = "EXTRA_STATION_NAME"
        const val EXTRA_TRAIN_ID = "EXTRA_TRAIN_ID"
        const val EXTRA_REQUEST_ID = "EXTRA_REQUEST_ID"
        const val EXTRA_ALERT_ID = "EXTRA_ALERT_ID"
        const val EXTRA_CIRCULAR_PLATFORM = "EXTRA_CIRCULAR_PLATFORM"

        fun startAlert(
            context: Context,
            alertType: String,
            title: String,
            message: String,
            stationName: String = "",
            trainId: String = "",
            requestId: String = "",
            alertId: String = "",
            circularPlatform: String = ""
        ) {
            val intent = Intent(context, AlertMediaPlayerService::class.java).apply {
                action = ACTION_PLAY_ALERT
                putExtra(EXTRA_ALERT_TYPE, alertType)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_MESSAGE, message)
                putExtra(EXTRA_STATION_NAME, stationName)
                putExtra(EXTRA_TRAIN_ID, trainId)
                putExtra(EXTRA_REQUEST_ID, requestId)
                putExtra(EXTRA_ALERT_ID, alertId)
                putExtra(EXTRA_CIRCULAR_PLATFORM, circularPlatform)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ContextCompat.startForegroundService(context, intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                // Fallback for background restrictions
                try { context.startService(intent) } catch (_: Exception) {}
            }
        }

        fun stopAlert(context: Context) {
            val intent = Intent(context, AlertMediaPlayerService::class.java).apply {
                action = ACTION_STOP_ALERT
            }
            try {
                context.startService(intent)
            } catch (_: Exception) {}
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initVibrator()
        acquireWakeLock()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_PLAY_ALERT

        when (action) {
            ACTION_STOP_ALERT -> {
                stopAlarmPlayback()
                stopSelf()
            }
            ACTION_PLAY_ALERT, ACTION_TEST_SIREN -> {
                val alertType = intent?.getStringExtra(EXTRA_ALERT_TYPE) ?: "2-3_STATION_WARNING"
                val title = intent?.getStringExtra(EXTRA_TITLE) ?: "🚨 DIVYANGJAN ASSISTANCE ALERT"
                val message = intent?.getStringExtra(EXTRA_MESSAGE) ?: "Train approaching destination station. Immediate assistance required."
                val platform = intent?.getStringExtra(EXTRA_CIRCULAR_PLATFORM) ?: "Platform 1 (+ Circular Line)"
                val trainId = intent?.getStringExtra(EXTRA_TRAIN_ID) ?: "T-245"

                val notification = buildForegroundNotification(title, message, alertType, platform, trainId)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(
                        NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                    )
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }

                playSirenAudio(alertType)
                startVibrationPattern(alertType)

                // Auto stop sound after 30 seconds if unacknowledged to prevent battery drain
                autoTimeoutJob?.cancel()
                autoTimeoutJob = serviceScope.launch {
                    delay(30000)
                    stopAlarmPlayback()
                }
            }
        }

        return START_NOT_STICKY
    }

    private fun playSirenAudio(alertType: String) {
        serviceScope.launch {
            try {
                stopMediaPlayerOnly()

                // Generate synthesized high-decibel siren WAV file according to alert type
                val wavFile = generateSirenWavFile(alertType)

                val player = MediaPlayer().apply {
                    val attributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setLegacyStreamType(AudioManager.STREAM_ALARM)
                        .build()

                    setAudioAttributes(attributes)
                    setDataSource(applicationContext, Uri.fromFile(wavFile))
                    isLooping = true
                    prepare()
                    start()
                }

                mediaPlayer = player
                isPlaying = true
            } catch (e: Exception) {
                // Fallback to system alarm ringtone
                playFallbackSystemAlarm()
            }
        }
    }

    private fun playFallbackSystemAlarm() {
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

            val player = MediaPlayer().apply {
                val attributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                setAudioAttributes(attributes)
                setDataSource(applicationContext, alarmUri)
                isLooping = true
                prepare()
                start()
            }
            mediaPlayer = player
            isPlaying = true
        } catch (_: Exception) {}
    }

    /**
     * Synthesizes authentic metro siren WAV PCM 16-bit 44.1kHz audio in application cache.
     * Different frequencies for:
     * - 2-3 Station Approach Warning: Pulsing sweep 750 Hz -> 1150 Hz
     * - 1 Station Critical: Rapid Hi-Lo 900 Hz / 1400 Hz
     * - Destination Arrival: Wailing Emergency Sweep 650 Hz -> 1350 Hz
     */
    private fun generateSirenWavFile(alertType: String): File {
        val cacheFile = File(cacheDir, "metro_siren_${alertType.lowercase()}.wav")
        val sampleRate = 44100
        val durationSeconds = 3.0 // 3-second seamless repeating loop
        val totalSamples = (sampleRate * durationSeconds).toInt()
        val pcmData = ShortArray(totalSamples)

        var phase = 0.0

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate.toDouble()
            val freq: Double
            val amp: Double

            when {
                alertType.contains("2-3", ignoreCase = true) || alertType.contains("APPROACH", ignoreCase = true) -> {
                    // 2-3 stations advance warning sweep
                    val cycleTime = (t % 0.6) / 0.6
                    freq = 750.0 + (400.0 * cycleTime)
                    val pulse = if (cycleTime < 0.8) 1.0 else 0.0
                    amp = 30000.0 * pulse
                }
                alertType.contains("CRITICAL", ignoreCase = true) || alertType.contains("1_STATION", ignoreCase = true) -> {
                    // Rapid Hi-Lo 1 station critical siren
                    val step = ((t / 0.15).toInt()) % 2
                    freq = if (step == 0) 900.0 else 1400.0
                    amp = 31000.0
                }
                else -> {
                    // Destination / Wailing Siren
                    val sweepPhase = (t % 1.0) / 1.0
                    freq = if (sweepPhase < 0.5) {
                        650.0 + (1300.0 - 650.0) * (sweepPhase * 2.0)
                    } else {
                        1300.0 - (1300.0 - 650.0) * ((sweepPhase - 0.5) * 2.0)
                    }
                    amp = 30000.0
                }
            }

            val sample = (sin(phase) * amp).toInt().toShort()
            pcmData[i] = sample
            phase += 2.0 * PI * freq / sampleRate
            if (phase > 2.0 * PI) phase -= 2.0 * PI
        }

        // Write standard RIFF WAV header + PCM 16-bit Mono bytes
        FileOutputStream(cacheFile).use { fos ->
            val dataBytes = totalSamples * 2
            val totalDataLen = dataBytes + 36
            val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)

            header.put("RIFF".toByteArray())
            header.putInt(totalDataLen)
            header.put("WAVE".toByteArray())
            header.put("fmt ".toByteArray())
            header.putInt(16) // SubChunk1Size (16 for PCM)
            header.putShort(1.toShort()) // AudioFormat (1 for PCM)
            header.putShort(1.toShort()) // NumChannels (1 = Mono)
            header.putInt(sampleRate)
            header.putInt(sampleRate * 2) // ByteRate (SampleRate * NumChannels * BitsPerSample/8)
            header.putShort(2.toShort()) // BlockAlign
            header.putShort(16.toShort()) // BitsPerSample
            header.put("data".toByteArray())
            header.putInt(dataBytes)

            fos.write(header.array())

            val byteBuf = ByteBuffer.allocate(dataBytes).order(ByteOrder.LITTLE_ENDIAN)
            for (s in pcmData) {
                byteBuf.putShort(s)
            }
            fos.write(byteBuf.array())
        }

        return cacheFile
    }

    private fun buildForegroundNotification(
        title: String,
        message: String,
        alertType: String,
        platform: String,
        trainId: String
    ): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopAlertIntent = Intent(this, AlertMediaPlayerService::class.java).apply {
            action = ACTION_STOP_ALERT
        }
        val stopAlertPendingIntent = PendingIntent.getService(
            this,
            1,
            stopAlertIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val isAdvance = alertType.contains("2-3", ignoreCase = true) || alertType.contains("APPROACH", ignoreCase = true)
        val alertHeader = if (isAdvance) "⚠️ 2-3 STATIONS ADVANCE WARNING" else "🚨 CRITICAL DESTINATION APPROACH"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("$alertHeader: $title")
            .setContentText("$message • $platform (Train $trainId)")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$message\n\n• Train: $trainId\n• Platform: $platform\n• Status: URGENT ASSISTANCE REQUIRED")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(openAppPendingIntent)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "ACKNOWLEDGE & STOP SIREN",
                stopAlertPendingIntent
            )
            .addAction(
                android.R.drawable.ic_dialog_info,
                "OPEN CONTROL VIEW",
                openAppPendingIntent
            )
            .setOngoing(true)
            .setAutoCancel(false)
            .build()
    }

    private fun startVibrationPattern(alertType: String) {
        try {
            val pattern = if (alertType.contains("2-3", ignoreCase = true)) {
                longArrayOf(0, 300, 150, 300, 150, 600)
            } else {
                longArrayOf(0, 200, 100, 200, 100, 200, 100, 400)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        } catch (_: Exception) {}
    }

    private fun initVibrator() {
        vibrator = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
            wakeLock = powerManager?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PinkLineAssist::SirenAlertWakeLock")
            wakeLock?.acquire(35000)
        } catch (_: Exception) {}
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Audible high-priority siren alarms for Divyangjan assistance on Delhi Metro Pink Line"
                enableVibration(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setBypassDnd(true)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    private fun stopMediaPlayerOnly() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.release()
            mediaPlayer = null
            isPlaying = false
        } catch (_: Exception) {}
    }

    private fun stopAlarmPlayback() {
        stopMediaPlayerOnly()
        try {
            vibrator?.cancel()
        } catch (_: Exception) {}
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {}
        try {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } catch (_: Exception) {}
    }

    override fun onDestroy() {
        stopAlarmPlayback()
        autoTimeoutJob?.cancel()
        super.onDestroy()
    }
}
