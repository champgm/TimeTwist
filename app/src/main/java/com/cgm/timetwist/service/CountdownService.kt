package com.cgm.timetwist.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.SoundPool
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.cgm.timetwist.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.util.Log
import com.cgm.timetwist.SoundPoolManager
import com.cgm.timetwist.VibrationManager


class CountdownService : Service() {
    private var sound = false
    private var vibration = false
    private var cancelled = false
    private var durationMillis = 0L
    private lateinit var vibrator: Vibrator

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "CountdownServiceChannel"
        private const val NOTIFICATION_CHANNEL_NAME = "Countdown Service"
        private const val NOTIFICATION_ID = 83210
    }

    override fun onCreate() {
        super.onCreate()
        vibrator = this.getSystemService(Vibrator::class.java)
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

    // Not 100% sure this is necessary.
    private val notificationId = 83210
    private fun startNotification() {
        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationChannel.enableVibration(true) // ensure this is on
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)

        val notification = Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(NOTIFICATION_CHANNEL_NAME)
            .setContentText("Counting down...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startNotification()
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

        // This is where the active timer is counted down
        CoroutineScope(Dispatchers.IO).launch {
            cancelled = false

            // Wake Lock might not be necessary with that window-on-flag thing
            // val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            // var wakeLock: PowerManager.WakeLock =
            //     powerManager.newWakeLock(
            //         PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ON_AFTER_RELEASE,
            //         "TimeTwist::CountdownService"
            //     )

            // Timer has started
            smallAlert()
            try {
                updateTimes()
                while (timeRemaining > 1000 && !cancelled) {
                    updateTimes()
                    alertDevice(this@CountdownService, timeRemaining)
                    delay(1000)
                }

                // Time has elapsed
                if (!cancelled) {
                    alertDevice(this@CountdownService, 0)
                }
            } finally {
                // if (wakeLock.isHeld) {
                //     wakeLock.release()
                // }
                stopForeground(STOP_FOREGROUND_REMOVE)
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
        stopForeground(STOP_FOREGROUND_REMOVE)
    }
}
