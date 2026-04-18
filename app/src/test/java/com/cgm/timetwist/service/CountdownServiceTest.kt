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
    fun `manual starts should emit the initial small alert`() = runTest {
        val service = createServiceHarness(backgroundScope).service
        val alerter = RecordingTimerAlerter()
        service.timerAlerter = alerter
        service.timeProvider = { 1_000L }

        val result = service.onStartCommand(
            countdownIntent(startTime = 1_000L, suppressStartAlert = false),
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
    fun `transition starts should suppress the initial small alert`() = runTest {
        val service = createServiceHarness(backgroundScope).service
        val alerter = RecordingTimerAlerter()
        service.timerAlerter = alerter
        service.timeProvider = { 1_000L }

        service.onStartCommand(
            countdownIntent(startTime = 1_000L, suppressStartAlert = true),
            0,
            1,
        )
        runCurrent()

        assertThat(alerter.smallAlerts).isEqualTo(0)
    }

    @Test
    fun `service should emit completion alert before teardown alert`() = runTest {
        val harness = createServiceHarness(backgroundScope)
        val service = harness.service
        val alerter = RecordingTimerAlerter()
        service.timerAlerter = alerter
        assertThat(alerter.bigAlerts).isEqualTo(0)

        service.emitCompletionAlert()
        assertThat(alerter.bigAlerts).isEqualTo(1)

        harness.controller.destroy()
        assertThat(alerter.bigAlerts).isEqualTo(2)
    }

    private fun createServiceHarness(scope: kotlinx.coroutines.CoroutineScope): ServiceHarness {
        val serviceController = Robolectric.buildService(CountdownService::class.java).create()
        val service = serviceController.get().also { it.serviceScope = scope }
        return ServiceHarness(controller = serviceController, service = service)
    }

    private fun countdownIntent(
        startTime: Long,
        durationMillis: Long = 35_000L,
        sound: Boolean = true,
        vibration: Boolean = true,
        intervalStuff: Boolean = true,
        suppressStartAlert: Boolean,
    ): Intent {
        return Intent(application, CountdownService::class.java)
            .putExtra("startTime", startTime)
            .putExtra("durationMillis", durationMillis)
            .putExtra("repeating", false)
            .putExtra("sound", sound)
            .putExtra("vibration", vibration)
            .putExtra("intervalStuff", intervalStuff)
            .putExtra("suppressStartAlert", suppressStartAlert)
    }

    private data class ServiceHarness(
        val controller: org.robolectric.android.controller.ServiceController<CountdownService>,
        val service: CountdownService,
    )

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
