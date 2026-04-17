package com.cgm.timetwist.service

import android.app.Application
import android.app.Service
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class CountdownServiceTest {
    private val application: Application = ApplicationProvider.getApplicationContext()

    @Test
    fun `formatCountdownTime should format minute and hour values`() {
        assertThat(formatCountdownTime(0L)).isEqualTo("00:00")
        assertThat(formatCountdownTime(65_000L)).isEqualTo("01:05")
        assertThat(formatCountdownTime(3_726_000L)).isEqualTo("01:02:06")
    }

    @Test
    fun `decideCountdownAlert should suppress periodic alerts when interval stuff is disabled`() {
        assertThat(decideCountdownAlert(30_000L, intervalStuff = false)).isEqualTo(CountdownAlert.NONE)
    }

    @Test
    fun `decideCountdownAlert should follow README cadence thresholds`() {
        assertThat(decideCountdownAlert(45_000L, intervalStuff = true)).isEqualTo(CountdownAlert.SMALL)
        assertThat(decideCountdownAlert(30_000L, intervalStuff = true)).isEqualTo(CountdownAlert.SMALL)
        assertThat(decideCountdownAlert(29_000L, intervalStuff = true)).isEqualTo(CountdownAlert.NONE)
        assertThat(decideCountdownAlert(0L, intervalStuff = true)).isEqualTo(CountdownAlert.BIG)
    }

    @Test
    fun `onStartCommand should create foreground state without crashing`() = runTest {
        val serviceController = Robolectric.buildService(CountdownService::class.java).create()
        val service = serviceController.get()
        val alerter = RecordingTimerAlerter()
        val clock = MutableClock(now = 1_000L)
        service.timerAlerter = alerter
        service.timeProvider = { clock.now }
        service.serviceScope = backgroundScope

        val result = service.onStartCommand(
            Intent(application, CountdownService::class.java)
                .putExtra("startTime", 1_000L)
                .putExtra("durationMillis", 35_000L)
                .putExtra("repeating", false)
                .putExtra("sound", true)
                .putExtra("vibration", true)
                .putExtra("intervalStuff", true),
            0,
            1,
        )

        assertThat(result).isEqualTo(Service.START_STICKY)
        assertThat(shadowOf(service).lastForegroundNotification).isNotNull()
        assertThat(shadowOf(service).lastForegroundNotificationId).isEqualTo(83210)

        runCurrent()
        assertThat(alerter.smallAlerts).isEqualTo(1)
    }

    @Test
    fun `service should emit final alert when countdown reaches zero`() = runTest {
        val serviceController = Robolectric.buildService(CountdownService::class.java).create()
        val service = serviceController.get()
        val alerter = RecordingTimerAlerter()
        val clock = MutableClock(now = 5_000L)
        service.timerAlerter = alerter
        service.timeProvider = { clock.now }
        service.serviceScope = backgroundScope

        service.onStartCommand(
            Intent(application, CountdownService::class.java)
                .putExtra("startTime", 5_000L)
                .putExtra("durationMillis", 2_000L)
                .putExtra("sound", false)
                .putExtra("vibration", false)
                .putExtra("intervalStuff", false),
            0,
            1,
        )
        runCurrent()

        clock.now = 7_000L
        advanceTimeBy(1_000L)
        runCurrent()

        assertThat(alerter.bigAlerts).isGreaterThanOrEqualTo(1)
    }

    private class MutableClock(var now: Long)

    private class RecordingTimerAlerter : TimerAlerter {
        var smallAlerts = 0
        var bigAlerts = 0

        override fun smallAlert(sound: Boolean, vibration: Boolean) {
            smallAlerts += 1
        }

        override fun bigAlert(sound: Boolean, vibration: Boolean) {
            bigAlerts += 1
        }
    }
}
