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


    fun bigVibrate(context: Context) {
        // Get the system service for the vibrator
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // Check if the device has a vibrator
        if (vibrator.hasVibrator()) {
            // Create a one-shot vibration effect
            val vibrationEffect = VibrationEffect.createOneShot(1000, myAmplitude)
            vibrator.vibrate(vibrationEffect)
//            Log.d("Vibrate", "Vibration triggered")
        }
    }

    fun smallVibrate(context: Context) {
        // Get the system service for the vibrator
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            // Create and start vibration
            val vibrationEffect = VibrationEffect.createWaveform(myPattern, myAmplitudes, -1)
            vibrator.vibrate(vibrationEffect)
        } else {
//            Log.d("Vibrate", "This device does not support vibration")
        }
    }

    private fun vibrateDevice(context: Context, durationMillis: Long, timeRemaining: Long) {
//        Log.d("Vibrate", "Checking if should vibrate...")
        if (timeRemaining <= 0) {
            bigVibrate(context)
            return
        }

        val everyXSeconds = if (timeRemaining < 60000) 5 else 15
        val secondsLeft = timeRemaining / 1000L;

        var shouldVibrate = secondsLeft > 0 && (secondsLeft % everyXSeconds) == 0L;
        if (!shouldVibrate) return

//        Log.d("Vibrate", "Vibrating with $secondsLeft seconds left")
        smallVibrate(context)
    }


    private val NOTIFICATION_ID = 83210 // Choose an ID that uniquely identifies your notification
    private fun startNotification() {
//        Log.d("startNotification", "Starting notification...")
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
//        Log.d("onStartCommand", "onStartCommand was called")
        startNotification()
        durationMillis = intent?.getLongExtra("durationMillis", 0L) ?: 0L
        val startTime = intent?.getLongExtra("startTime", 0L) ?: 0L
        var currentTime = 0L
        var elapsedTime = 0L
        var timeRemaining = 0L

        fun updateTimes() {
            currentTime = System.currentTimeMillis()
            elapsedTime = currentTime - startTime
            timeRemaining = durationMillis - elapsedTime
//            Log.e("updateTimes", "===========================")
//            Log.e("updateTimes", "startTime: $startTime")
//            Log.e("updateTimes", "currentTime: $currentTime")
//            Log.e("updateTimes", "elapsedTime: $elapsedTime")
//            Log.e("updateTimes", "timeRemaining: $timeRemaining")
//            Log.e("updateTimes", "===========================")
        }

        Log.d("onStartCommand", "Starting coroutine...")
        CoroutineScope(Dispatchers.IO).launch {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            var wakeLock: PowerManager.WakeLock =
                powerManager.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ON_AFTER_RELEASE,
                    "TimeTwist::CountdownService"
                )
            wakeLock.acquire(durationMillis + (10000))

            smallVibrate(this@CountdownService)
            try {
                updateTimes()
                while (timeRemaining > 1000) {
                    // Log.d("onStartCommand", "Time Remaining: ${timeRemaining}ms")
                    updateTimes()
                    vibrateDevice(this@CountdownService, durationMillis, timeRemaining)
                    delay(1000)
                }

                // Time has elapsed
                vibrateDevice(this@CountdownService, durationMillis, 0)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            } finally {
                if (wakeLock.isHeld) {
                    wakeLock.release()
                }
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
        stopForeground(STOP_FOREGROUND_REMOVE)
    }
}
