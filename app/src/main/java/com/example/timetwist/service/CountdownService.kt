package com.example.timetwist.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import com.example.timetwist.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class CountdownService : Service() {
    fun vibrateDevice(context: Context, timeRemaining: Long) {
        Log.d("Vibrate", "Attempting to vibrate...")

        // Log context information
        Log.d("Vibrate", "Context: $context")

        // Get the system service for the vibrator
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // Check if the device has a vibrator and log it
        if (vibrator.hasVibrator()) {
            Log.d("Vibrate", "Vibrator exists")

            // Create a one-shot vibration effect
            val vibrationEffect = VibrationEffect.createOneShot(
                500, // Duration in milliseconds
                255 // Amplitude
            )

            // Log VibrationEffect details
            Log.d("Vibrate", "VibrationEffect: $vibrationEffect")

            // Trigger the vibration
            vibrator.vibrate(vibrationEffect)

            Log.d("Vibrate", "Vibration triggered")
        } else {
            Log.d("Vibrate", "This device does not support vibration")
        }
    }


    private val NOTIFICATION_ID = 83210 // Choose an ID that uniquely identifies your notification
    private fun startNotification() {
        Log.d("startNotification", "Starting notification...")
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
        Log.d("onStartCommand", "onStartCommand was called")
        startNotification()
        val durationMillis = intent?.getLongExtra("durationMillis", 0L) ?: 0L
        val startTime = System.currentTimeMillis()


        var currentTime = System.currentTimeMillis()
        var elapsedTime = currentTime - startTime
        var timeRemaining = durationMillis - elapsedTime
        Log.d("onStartCommand", "Starting coroutine...")
        CoroutineScope(Dispatchers.IO).launch {
            while (timeRemaining > 0) {
                currentTime = System.currentTimeMillis()
                elapsedTime = currentTime - startTime
                timeRemaining = durationMillis - elapsedTime
                Log.d("onStartCommand", "Time Remaining: ${timeRemaining}ms")
                if (timeRemaining <= 0) {
                    //  vibrateDevice(this@CountdownService, timeRemaining)
                    break
                } else {

                }
                delay(1000)  // Wait for 1 second before checking again
            }

            // Time has elapsed, perform your action (like vibration)
            vibrateDevice(this@CountdownService, 0)
            stopForeground(true)
            stopSelf()
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
    }
}
