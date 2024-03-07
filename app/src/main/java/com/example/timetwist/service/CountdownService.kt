package com.example.timetwist.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import com.example.timetwist.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CountdownService : Service() {
    public var running = false;
    private var cancelled = false;
    private var durationMillis = 0L
    private val maxAmplitude = 255
    private val myAmplitude = 200
    private val pause = 200L
    private val dott = 200L
    private val dash = 500L
    private val speedMultiplier = 4L
    private val spaceBetweenDigits = 500L
    private val ok = 100L
    private val okPause = 50L
    private val myPattern = longArrayOf(0, ok, okPause, ok)
    private val myAmplitudes = intArrayOf(0, myAmplitude, 0, myAmplitude)


    private fun bigVibrate(context: Context) {
        // Get the system service for the vibrator
        val vibrator = context.getSystemService(Vibrator::class.java)

        // Check if the device has a vibrator
        if (vibrator.hasVibrator()) { // Create a one-shot vibration effect
            val vibrationEffect = VibrationEffect.createOneShot(1000, myAmplitude)
            vibrator.vibrate(vibrationEffect)
        }
    }

    private fun smallVibrate(context: Context) {
        // Get the system service for the vibrator
        val vibrator = context.getSystemService(Vibrator::class.java)

        // Check if the device has a vibrator
        if (vibrator?.hasVibrator() == true) { // Create and start vibration
            val vibrationEffect = VibrationEffect.createWaveform(myPattern, myAmplitudes, -1)
            vibrator.vibrate(vibrationEffect)
        }
    }

    private fun vibrateDevice(context: Context, timeRemaining: Long) {
        // Vibrate if timer is done
        if (timeRemaining <= 0) {
            bigVibrate(context)
            return
        }

        // Vibrate every 15 seconds if less than 1 minute remaining
        // Otherwise, vibrate every 5 seconds
        val everyXSeconds = if (timeRemaining < 60000) 5 else 15
        val secondsLeft = timeRemaining / 1000L;

        // Check if it's time to vibrate
        val shouldVibrate = secondsLeft > 0 && (secondsLeft % everyXSeconds) == 0L;
        if (!shouldVibrate) return

        smallVibrate(context)
    }

    // Not 100% sure this is necessary.
    private val NOTIFICATION_ID = 83210
    private fun startNotification() {
        val notificationChannel = NotificationChannel(
            "CountdownServiceChannel",
            "Countdown Service",
            NotificationManager.IMPORTANCE_HIGH
        )
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)

        val notification = Notification.Builder(this, "CountdownServiceChannel")
            .setContentTitle("Countdown Service")
            .setContentText("Counting down...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startNotification()
        // val repeating = intent?.getBooleanExtra("repeating", false) ?: false
        durationMillis = intent?.getLongExtra("durationMillis", 0L) ?: 0L
        val startTime = intent?.getLongExtra("startTime", 0L) ?: 0L
        var currentTime = 0L
        var elapsedTime = 0L
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
            smallVibrate(this@CountdownService)
            try {
                updateTimes()
                while (timeRemaining > 1000 && !cancelled) {
                    updateTimes()
                    vibrateDevice(this@CountdownService, timeRemaining)
                    delay(1000)
                }

                // Time has elapsed
                if (!cancelled) {
                    vibrateDevice(this@CountdownService, 0)
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
        super.onDestroy()
        durationMillis = 0
        cancelled = true
        stopForeground(STOP_FOREGROUND_REMOVE)
    }
}
