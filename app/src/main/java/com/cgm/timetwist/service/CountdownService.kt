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

internal const val SMALL_ALERT_INTERVAL_SECONDS = 5L
internal const val BIG_ALERT_INTERVAL_SECONDS = 15L
internal const val INTERMITTENT_ALERT_THRESHOLD_MILLIS = 30000L
private const val COUNTDOWN_TICK_MILLIS = 1000L

internal enum class CountdownAlert {
    NONE,
    SMALL,
    BIG,
}

internal interface TimerAlerter {
    fun smallAlert(sound: Boolean, vibration: Boolean)
    fun bigAlert(sound: Boolean, vibration: Boolean)
}

internal object DeviceTimerAlerter : TimerAlerter {
    override fun smallAlert(sound: Boolean, vibration: Boolean) {
        if (sound) SoundPoolManager.playClickSound()
        if (vibration) VibrationManager.vibrateClick()
    }

    override fun bigAlert(sound: Boolean, vibration: Boolean) {
        if (sound) SoundPoolManager.playCrunchSound()
        if (vibration) VibrationManager.vibrateHeavyClick()
    }
}

internal fun formatCountdownTime(millis: Long): String {
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

internal fun decideCountdownAlert(
    timeRemaining: Long,
    intervalStuff: Boolean,
): CountdownAlert {
    if (timeRemaining <= 0L) {
        return CountdownAlert.BIG
    }
    if (!intervalStuff) {
        return CountdownAlert.NONE
    }

    val alertIntervalSeconds =
        if (timeRemaining <= INTERMITTENT_ALERT_THRESHOLD_MILLIS) {
            SMALL_ALERT_INTERVAL_SECONDS
        } else {
            BIG_ALERT_INTERVAL_SECONDS
        }
    val secondsLeft = timeRemaining / 1000L
    return if (secondsLeft > 0 && secondsLeft % alertIntervalSeconds == 0L) {
        CountdownAlert.SMALL
    } else {
        CountdownAlert.NONE
    }
}

class CountdownService : Service() {
    private var sound = false
    private var vibration = false
    private var intervalStuff = false
    private var cancelled = false
    private var durationMillis = 0L
    private lateinit var vibrator: Vibrator
    private var ongoingActivity: OngoingActivity? = null
    internal var timeProvider: () -> Long = System::currentTimeMillis
    internal var timerAlerter: TimerAlerter = DeviceTimerAlerter
    internal var serviceScope: CoroutineScope = CoroutineScope(Dispatchers.IO)

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
        timerAlerter.bigAlert(sound = sound, vibration = vibration)
        Log.d("VibrationTest", "Big alert triggered")
    }

    private fun smallAlert() {
        timerAlerter.smallAlert(sound = sound, vibration = vibration)
        Log.d("VibrationTest", "Small alert triggered")
    }

    internal fun alertDecision(timeRemaining: Long): CountdownAlert {
        return decideCountdownAlert(timeRemaining = timeRemaining, intervalStuff = intervalStuff)
    }

    private fun alertDevice(timeRemaining: Long): CountdownAlert {
        return when (val alert = alertDecision(timeRemaining)) {
            CountdownAlert.BIG -> {
                bigAlert()
                alert
            }

            CountdownAlert.SMALL -> {
                smallAlert()
                alert
            }

            CountdownAlert.NONE -> alert
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        sound = intent?.getBooleanExtra("sound", false) ?: false
        vibration = intent?.getBooleanExtra("vibration", false) ?: false
        intervalStuff = intent?.getBooleanExtra("intervalStuff", false) ?: false
        durationMillis = intent?.getLongExtra("durationMillis", 0L) ?: 0L
        val startTime = intent?.getLongExtra("startTime", 0L) ?: 0L
        var currentTime: Long
        var elapsedTime: Long
        var timeRemaining = 0L

        fun updateTimes() {
            currentTime = timeProvider()
            elapsedTime = currentTime - startTime
            timeRemaining = durationMillis - elapsedTime
        }

        updateTimes()
        setupOngoingActivity(durationMillis)

        serviceScope.launch {
            cancelled = false
            smallAlert()
            updateStatus(timeRemaining)
            try {
                while (timeRemaining > COUNTDOWN_TICK_MILLIS && !cancelled) {
                    delay(COUNTDOWN_TICK_MILLIS)
                    updateTimes()
                    updateStatus(timeRemaining)
                    alertDevice(timeRemaining)
                }

                if (!cancelled) {
                    alertDevice(0)
                }
            } finally {
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
            enableVibration(false)
        }
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
    }

    private fun setupOngoingActivity(initialDurationMillis: Long) {
        val notificationBuilder = createNotificationBuilder()
        val notification = notificationBuilder.build()

        startForeground(NOTIFICATION_ID, notification)

        val icon: Icon = Icon.createWithResource(applicationContext, R.drawable.timer_outline)
        val ongoingActivityBuilder =
            OngoingActivity.Builder(applicationContext, NOTIFICATION_ID, notificationBuilder)
                .setStaticIcon(icon)
                .setTouchIntent(createActivityPendingIntent())

        ongoingActivity = ongoingActivityBuilder.build()
        ongoingActivity?.apply(applicationContext)
        updateStatus(initialDurationMillis)
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
            .setContentIntent(createActivityPendingIntent())
    }

    private fun createActivityPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun updateStatus(timeRemainingMillis: Long) {
        val status = Status.Builder()
            .addTemplate("Time left: #time#")
            .addPart("time", Status.TextPart(formatCountdownTime(timeRemainingMillis)))
            .build()
        ongoingActivity?.update(applicationContext, status)
    }
}
