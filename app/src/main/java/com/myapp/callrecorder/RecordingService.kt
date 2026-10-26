package com.myapp.callrecorder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.myapp.callrecorder.data.AppDatabase
import com.myapp.callrecorder.data.CallRecording
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecordingService : Service() {

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_NUMBER = "EXTRA_NUMBER"
        const val EXTRA_DIRECTION = "EXTRA_DIRECTION"

        private const val CHANNEL_ID = "call_recording_silent_channel"
        private const val NOTIFICATION_ID = 101
    }

    private var recorder: MediaRecorder? = null
    private var currentFilePath: String = ""
    private var currentNumber: String = "Unknown"
    private var currentDirection: String = "INCOMING"
    private var startTimeMillis: Long = 0L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                currentNumber = intent.getStringExtra(EXTRA_NUMBER) ?: "Unknown"
                currentDirection = intent.getStringExtra(EXTRA_DIRECTION) ?: "INCOMING"
                startForeground(NOTIFICATION_ID, buildSilentNotification())
                startRecording()
            }
            ACTION_STOP -> {
                stopRecordingAndSave()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    /**
     * Android requires a visible notification for any foreground service
     * (this cannot be fully removed on Android 8+ - it's an OS-level rule,
     * not an app setting). To keep it as unobtrusive as possible we:
     *  - use IMPORTANCE_MIN so it never makes a sound, never pops up, and
     *    collapses into the status bar icon area only
     *  - give it a neutral, low-key label
     */
    private fun buildSilentNotification(): android.app.Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Background activity",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Call Notes")
            .setContentText("Running")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .build()
    }

    private fun startRecording() {
        val dir = File(getExternalFilesDir(null), "recordings")
        if (!dir.exists()) dir.mkdirs()

        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        currentFilePath = File(dir, "call_${stamp}.m4a").absolutePath
        startTimeMillis = System.currentTimeMillis()

        recorder = MediaRecorder().apply {
            // VOICE_COMMUNICATION captures both sides of the call on devices that allow it.
            // Many modern OEMs block this at the OS level regardless of permissions.
            setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(currentFilePath)
            try {
                prepare()
                start()
            } catch (e: Exception) {
                // Device/OS blocked call-audio recording - fails safely, nothing is saved
                release()
                recorder = null
            }
        }
    }

    private fun stopRecordingAndSave() {
        val rec = recorder ?: return
        try {
            rec.stop()
        } catch (_: Exception) {
            // stop() can throw if recording was too short or never really started
        }
        rec.release()
        recorder = null

        val durationSec = ((System.currentTimeMillis() - startTimeMillis) / 1000).toInt()
        if (durationSec <= 0 || currentFilePath.isEmpty()) return

        val entry = CallRecording(
            number = currentNumber,
            timestamp = startTimeMillis,
            durationSec = durationSec,
            filePath = currentFilePath,
            direction = currentDirection
        )

        CoroutineScope(Dispatchers.IO).launch {
            AppDatabase.getInstance(applicationContext).callRecordingDao().insert(entry)
        }
    }
}
