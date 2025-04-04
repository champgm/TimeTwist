package com.cgm.timetwist.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.IBinder
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.ongoing.Status
import com.cgm.timetwist.R
import com.cgm.timetwist.SoundPoolManager
import com.cgm.timetwist.VibrationManager
import com.cgm.timetwist.presentation.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class CountdownService : Service() {
    private var sound = false
    private var vibration = false
    private var cancelled = false
    private var durationMillis = 0L
    private lateinit var vibrator: Vibrator
    private var ongoingActivity: OngoingActivity? = null

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "CountdownServiceChannel"
        private const val NOTIFICATION_CHANNEL_NAME = "Countdown Service"
        private const val NOTIFICATION_ID = 83210
    }

    override fun onCreate() {
        super.onCreate()
        vibrator = this.getSystemService(Vibrator::class.java)
        createNotificationChannel()
    }

    private fun bigAlert() {
        if (sound) SoundPoolManager.playCrunchSound()
        if (vibration) VibrationManager.vibrateHeavyClick()
        Log.d("VibrationTest", "Big alert triggered")
    }

    private fun smallAlert() {
        if (sound) SoundPoolManager.playClickSound()
        if (vibration) VibrationManager.vibrateClick()
        Log.d("VibrationTest", "Small alert triggered")
    }

    private fun alertDevice(context: Context, timeRemaining: Long) {
        // Alert if timer is done
        if (timeRemaining <= 0) {
            bigAlert()
            return
        }

        // Alert every 15 seconds if less than 1 minute remaining
        // Otherwise, alert every 5 seconds
        val everyXSeconds = if (timeRemaining < 60000) 5 else 15
        val secondsLeft = timeRemaining / 1000L

        // Check if it's time to alert
        val shouldAlert = secondsLeft > 0 && (secondsLeft % everyXSeconds) == 0L
        if (!shouldAlert) return

        smallAlert()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        sound = intent?.getBooleanExtra("sound", false) ?: false
        vibration = intent?.getBooleanExtra("vibration", false) ?: false
        durationMillis = intent?.getLongExtra("durationMillis", 0L) ?: 0L
        val startTime = intent?.getLongExtra("startTime", 0L) ?: 0L
        var currentTime: Long
        var elapsedTime: Long
        var timeRemaining = 0L

        fun updateTimes() {
            currentTime = System.currentTimeMillis()
            elapsedTime = currentTime - startTime
            timeRemaining = durationMillis - elapsedTime
        }

        // Set up Ongoing Activity
        setupOngoingActivity(durationMillis)

        // This is where the active timer is counted down
        CoroutineScope(Dispatchers.IO).launch {
            cancelled = false

            // Timer has started
            smallAlert()
            updateStatus(timeRemaining)
            try {
                updateTimes()
                while (timeRemaining > 1000 && !cancelled) {
                    updateTimes()
                    updateStatus(timeRemaining)
                    alertDevice(this@CountdownService, timeRemaining)
                    delay(1000)
                }

                // Time has elapsed
                if (!cancelled) {
                    alertDevice(this@CountdownService, 0)
                }
            } finally {
                // OngoingActivity handles stopping foreground implicitly when status is cleared
                stopSelf()
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        bigAlert()
        super.onDestroy()
        durationMillis = 0
        cancelled = true
        ongoingActivity = null
    }

    private fun createNotificationChannel() {
        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows the ongoing timer"
            // Vibration is handled by the alerts, not the channel itself for ongoing
            enableVibration(false)
        }
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
    }

    private fun setupOngoingActivity(initialDurationMillis: Long) {
        val notificationBuilder = createNotificationBuilder()
        val notification = notificationBuilder.build()

        Log.d("TimerDebug", "About to call startForeground.") // ADD THIS
        startForeground(NOTIFICATION_ID, notification)
        Log.d("TimerDebug", "startForeground finished.")

        val icon: Icon = Icon.createWithResource(applicationContext, R.drawable.timer_outline)
        val ongoingActivityBuilder =
            OngoingActivity.Builder(applicationContext, NOTIFICATION_ID, notificationBuilder)
                .setStaticIcon(icon)
                .setTouchIntent(createActivityPendingIntent())

        Log.d("setupOngoingActivity", "Building ongoing activity...")
        ongoingActivity = ongoingActivityBuilder.build()
        Log.d("setupOngoingActivity", "Ongoing activity built")

        Log.d("setupOngoingActivity", "Applying context...")
        ongoingActivity?.apply(applicationContext)
        Log.d("setupOngoingActivity", "Context applied")

        Log.d("setupOngoingActivity", "Updating status...")
        updateStatus(initialDurationMillis)
        Log.d("setupOngoingActivity", "Status updated")
    }

    private fun createNotificationBuilder(): NotificationCompat.Builder {
        val icon: Icon = Icon.createWithResource(applicationContext, R.drawable.timer_outline)
        val iconCompat =
            IconCompat.createWithResource(applicationContext, R.drawable.timer_outline)

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setLargeIcon(icon)
            .setSmallIcon(iconCompat)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Timer running...")
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            // Add the intent to open the app when the notification is tapped
            .setContentIntent(createActivityPendingIntent())
    }

    // Creates PendingIntent to launch your MainActivity
    private fun createActivityPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    // Helper function to format milliseconds into MM:SS or HH:MM:SS
    private fun formatTime(millis: Long): String {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    private fun updateStatus(timeRemainingMillis: Long) {
        val timeStr = formatTime(timeRemainingMillis)
        val status = Status.Builder()
            .addTemplate("Time left: #time#")
            .addPart("time", Status.TextPart(timeStr))
            .build()
        ongoingActivity?.update(applicationContext, status)
    }
}
