package com.cgm.timetwist.presentation

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.cgm.timetwist.service.TimeDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class TimerViewModelTest {
    private val application: Application = ApplicationProvider.getApplicationContext()
    private val context: Context = application.applicationContext

    @Before
    fun setUp() {
        clearPreferences()
    }

    @After
    fun tearDown() {
        clearPreferences()
    }

    @Test
    fun `constructor should expose default timer state`() = runTest {
        val viewModel = createViewModel(this)

        assertThat(viewModel.timer0.value).isEqualTo(
            TimeDetails(
                timerId = "timer0",
                durationMillis = 33000L,
                repeating = false,
                sound = false,
                vibration = true,
                intervalStuff = true,
            )
        )
        assertThat(viewModel.timer1.value).isEqualTo(
            TimeDetails(
                timerId = "timer1",
                durationMillis = 5000L,
                repeating = false,
                sound = true,
                vibration = true,
                intervalStuff = true,
            )
        )
        assertThat(viewModel.timer2.value).isEqualTo(
            TimeDetails(
                timerId = "timer2",
                durationMillis = 310000L,
                repeating = false,
                sound = true,
                vibration = true,
                intervalStuff = true,
            )
        )
    }

    @Test
    fun `constructor should load persisted timers`() = runTest {
        saveTimerDetails(
            context = context,
            timerId = "timer1",
            details = TimeDetails(
                timerId = "timer1",
                durationMillis = 42_000L,
                repeating = true,
                vibration = false,
                sound = false,
                intervalStuff = false,
            )
        )

        val viewModel = createViewModel(this)

        assertThat(viewModel.timer1.value.durationMillis).isEqualTo(42_000L)
        assertThat(viewModel.timer1.value.repeating).isTrue()
        assertThat(viewModel.timer1.value.vibration).isFalse()
        assertThat(viewModel.timer1.value.sound).isFalse()
        assertThat(viewModel.timer1.value.intervalStuff).isFalse()
    }

    @Test
    fun `getTimer should return selected timer and reject invalid ids`() = runTest {
        val viewModel = createViewModel(this)

        assertThat(viewModel.getTimer("timer2").timerId).isEqualTo("timer2")
        assertThatThrownBy { viewModel.getTimer("timer9") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Invalid timerId")
    }

    @Test
    fun `updateTimerDuration should update state and persisted values`() = runTest {
        val viewModel = createViewModel(this)

        viewModel.updateTimerDuration(
            id = "timer0",
            newDurationMillis = 91_000L,
            newRepeating = true,
            newSound = true,
            newVibration = false,
            newIntervalStuff = false,
        )

        assertThat(viewModel.timer0.value.durationMillis).isEqualTo(91_000L)
        assertThat(viewModel.timer0.value.repeating).isTrue()
        assertThat(viewModel.timer1.value.durationMillis).isEqualTo(5000L)

        val restored = getTimerDetails(context, "timer0")
        assertThat(restored?.durationMillis).isEqualTo(91_000L)
        assertThat(restored?.repeating).isTrue()
        assertThat(restored?.sound).isTrue()
        assertThat(restored?.vibration).isFalse()
        assertThat(restored?.intervalStuff).isFalse()
    }

    @Test
    fun `startTimer should stop other timers and capture service extras`() = runTest {
        val clock = MutableClock(now = 10_000L)
        val serviceController = RecordingTimerServiceController()
        val viewModel = createViewModel(this, clock, serviceController)

        viewModel.startTimer("timer1", context, backgroundScope)

        assertThat(viewModel.timer0.value.started).isFalse()
        assertThat(viewModel.timer1.value.started).isTrue()
        assertThat(viewModel.timer2.value.started).isFalse()
        assertThat(viewModel.timer1.value.startTime).isEqualTo(10_000L)
        assertThat(viewModel.timer1.value.timeRemaining).isEqualTo(5000L)
        assertThat(serviceController.stopCalls).isEqualTo(1)
        assertThat(serviceController.startRequests).hasSize(1)

        val request = serviceController.startRequests.single()
        assertThat(request.startTime).isEqualTo(10_000L)
        assertThat(request.durationMillis).isEqualTo(5000L)
        assertThat(request.repeating).isFalse()
        assertThat(request.sound).isTrue()
        assertThat(request.vibration).isTrue()
        assertThat(request.intervalStuff).isTrue()
    }

    @Test
    fun `stopTimers should clear active timers and stop the service`() = runTest {
        val serviceController = RecordingTimerServiceController()
        val viewModel = createViewModel(this, serviceController = serviceController)

        viewModel.startTimer("timer0", context, backgroundScope)
        viewModel.stopTimers(context)

        assertThat(viewModel.timer0.value.started).isFalse()
        assertThat(viewModel.timer1.value.started).isFalse()
        assertThat(viewModel.timer2.value.started).isFalse()
        assertThat(serviceController.stopCalls).isEqualTo(2)
    }

    @Test
    fun `completed non repeating timer should stop`() = runTest {
        val clock = MutableClock(now = 0L)
        val serviceController = RecordingTimerServiceController()
        val viewModel = createViewModel(this, clock, serviceController)

        viewModel.updateTimerDuration(
            id = "timer0",
            newDurationMillis = 2_000L,
            newRepeating = false,
            newSound = false,
            newVibration = true,
            newIntervalStuff = true,
        )
        viewModel.startTimer("timer0", context, backgroundScope)

        clock.now = 2_000L
        advanceTimeBy(2_000L)
        runCurrent()

        assertThat(viewModel.timer0.value.started).isFalse()
        assertThat(viewModel.timer0.value.timeRemaining).isLessThanOrEqualTo(0L)
        assertThat(serviceController.startRequests).hasSize(1)
        assertThat(serviceController.stopCalls).isEqualTo(2)
    }

    @Test
    fun `completed repeating timer should restart with saved duration`() = runTest {
        val clock = MutableClock(now = 0L)
        val serviceController = RecordingTimerServiceController()
        val viewModel = createViewModel(this, clock, serviceController)

        viewModel.updateTimerDuration(
            id = "timer2",
            newDurationMillis = 2_000L,
            newRepeating = true,
            newSound = true,
            newVibration = false,
            newIntervalStuff = false,
        )
        viewModel.startTimer("timer2", context, backgroundScope)

        clock.now = 2_000L
        advanceTimeBy(2_000L)
        runCurrent()

        assertThat(viewModel.timer2.value.started).isTrue()
        assertThat(viewModel.timer2.value.startTime).isEqualTo(2_000L)
        assertThat(viewModel.timer2.value.durationMillis).isEqualTo(2_000L)
        assertThat(viewModel.timer2.value.repeating).isTrue()
        assertThat(serviceController.startRequests).hasSize(2)
        assertThat(serviceController.startRequests.last().durationMillis).isEqualTo(2_000L)
    }

    private fun createViewModel(
        scope: TestScope,
        clock: MutableClock = MutableClock(0L),
        serviceController: RecordingTimerServiceController = RecordingTimerServiceController(),
    ): TimerViewModel {
        return TimerViewModel(
            application = application,
            timeProvider = { clock.now },
            timerServiceController = serviceController,
            timerCoroutineScope = scope.backgroundScope,
        )
    }

    private fun clearPreferences() {
        context.getSharedPreferences("time_twist_preferences", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    private class MutableClock(var now: Long)

    private class RecordingTimerServiceController : TimerServiceController {
        val startRequests = mutableListOf<TimerServiceRequest>()
        var stopCalls = 0

        override fun start(
            context: Context,
            coroutineScope: CoroutineScope,
            request: TimerServiceRequest,
        ) {
            startRequests += request
        }

        override fun stop(context: Context, coroutineScope: CoroutineScope) {
            stopCalls += 1
        }
    }
}
