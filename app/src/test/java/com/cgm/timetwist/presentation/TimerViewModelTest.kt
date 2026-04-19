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
            ),
        )
        assertThat(viewModel.timer1.value).isEqualTo(
            TimeDetails(
                timerId = "timer1",
                durationMillis = 5000L,
                repeating = false,
                sound = true,
                vibration = true,
                intervalStuff = true,
            ),
        )
        assertThat(viewModel.timer2.value).isEqualTo(
            TimeDetails(
                timerId = "timer2",
                durationMillis = 310000L,
                repeating = false,
                sound = true,
                vibration = true,
                intervalStuff = true,
            ),
        )
        assertThat(viewModel.transition0To2.value).isEqualTo(TransitionState0To2.DEFAULT)
        assertThat(viewModel.transition1To2.value).isEqualTo(TransitionState1To2.DEFAULT)
    }

    @Test
    fun `constructor should load persisted timers and transitions`() = runTest {
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
            ),
        )
        saveTransitionState0To2(context, TransitionState0To2.TWO_TO_ZERO)
        saveTransitionState1To2(context, TransitionState1To2.ONE_TO_TWO)

        val viewModel = createViewModel(this)

        assertThat(viewModel.timer1.value.durationMillis).isEqualTo(42_000L)
        assertThat(viewModel.timer1.value.repeating).isTrue()
        assertThat(viewModel.timer1.value.vibration).isFalse()
        assertThat(viewModel.timer1.value.sound).isFalse()
        assertThat(viewModel.timer1.value.intervalStuff).isFalse()
        assertThat(viewModel.transition0To2.value).isEqualTo(TransitionState0To2.TWO_TO_ZERO)
        assertThat(viewModel.transition1To2.value).isEqualTo(TransitionState1To2.ONE_TO_TWO)
    }

    @Test
    fun `constructor should default invalid transition persistence`() = runTest {
        context.getSharedPreferences("time_twist_preferences", Context.MODE_PRIVATE)
            .edit()
            .putString("transition_0_2", "BROKEN")
            .putString("transition_1_2", "BROKEN")
            .commit()

        val viewModel = createViewModel(this)

        assertThat(viewModel.transition0To2.value).isEqualTo(TransitionState0To2.DEFAULT)
        assertThat(viewModel.transition1To2.value).isEqualTo(TransitionState1To2.DEFAULT)
    }

    @Test
    fun `constructor should repair persisted directional dual exit from timer2`() = runTest {
        saveTransitionState0To2(context, TransitionState0To2.TWO_TO_ZERO)
        saveTransitionState1To2(context, TransitionState1To2.TWO_TO_ONE)

        val viewModel = createViewModel(this)

        assertThat(viewModel.transition0To2.value).isEqualTo(TransitionState0To2.DEFAULT)
        assertThat(viewModel.transition1To2.value).isEqualTo(TransitionState1To2.DEFAULT)
        assertThat(getTransitionState0To2(context)).isEqualTo(TransitionState0To2.DEFAULT)
        assertThat(getTransitionState1To2(context)).isEqualTo(TransitionState1To2.DEFAULT)
    }

    @Test
    fun `constructor should repair persisted repeat dual exit from timer2`() = runTest {
        saveTransitionState0To2(context, TransitionState0To2.ZERO_TWO_REPEAT)
        saveTransitionState1To2(context, TransitionState1To2.ONE_TWO_REPEAT)

        val viewModel = createViewModel(this)

        assertThat(viewModel.transition0To2.value).isEqualTo(TransitionState0To2.DEFAULT)
        assertThat(viewModel.transition1To2.value).isEqualTo(TransitionState1To2.DEFAULT)
        assertThat(getTransitionState0To2(context)).isEqualTo(TransitionState0To2.DEFAULT)
        assertThat(getTransitionState1To2(context)).isEqualTo(TransitionState1To2.DEFAULT)
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
        assertThat(request.suppressStartAlert).isFalse()
        assertThat(getActiveTimerState(context)).isEqualTo(
            ActiveTimerState(timerId = "timer1", startTime = 10_000L),
        )
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
        assertThat(getActiveTimerState(context)).isNull()
    }

    @Test
    fun `constructor should restore a still active timer from persisted runtime state`() = runTest {
        val clock = MutableClock(now = 12_000L)
        saveTimerDetails(
            context = context,
            timerId = "timer2",
            details = TimeDetails(
                timerId = "timer2",
                durationMillis = 10_000L,
                repeating = false,
                vibration = true,
                sound = true,
                intervalStuff = true,
            ),
        )
        saveActiveTimerState(
            context = context,
            state = ActiveTimerState(timerId = "timer2", startTime = 5_000L),
        )

        val viewModel = createViewModel(this, clock)

        assertActiveTimer(viewModel, "timer2")
        assertThat(viewModel.timer2.value.startTime).isEqualTo(5_000L)
        assertThat(viewModel.timer2.value.elapsedTime).isEqualTo(7_000L)
        assertThat(viewModel.timer2.value.timeRemaining).isEqualTo(3_000L)
    }

    @Test
    fun `constructor should clear expired persisted runtime state and stop the service`() = runTest {
        val clock = MutableClock(now = 15_000L)
        val serviceController = RecordingTimerServiceController()
        saveTimerDetails(
            context = context,
            timerId = "timer0",
            details = TimeDetails(
                timerId = "timer0",
                durationMillis = 4_000L,
                repeating = false,
                vibration = true,
                sound = false,
                intervalStuff = true,
            ),
        )
        saveActiveTimerState(
            context = context,
            state = ActiveTimerState(timerId = "timer0", startTime = 5_000L),
        )

        val viewModel = createViewModel(this, clock, serviceController)

        assertThat(viewModel.timer0.value.started).isFalse()
        assertThat(viewModel.timer1.value.started).isFalse()
        assertThat(viewModel.timer2.value.started).isFalse()
        assertThat(getActiveTimerState(context)).isNull()
        assertThat(serviceController.stopCalls).isEqualTo(1)
    }

    @Test
    fun `cycleTransition0To2 should visit every state and persist it`() = runTest {
        val viewModel = createViewModel(this)

        viewModel.cycleTransition0To2()
        assertThat(viewModel.transition0To2.value).isEqualTo(TransitionState0To2.ZERO_TO_TWO)
        assertThat(getTransitionState0To2(context)).isEqualTo(TransitionState0To2.ZERO_TO_TWO)

        viewModel.cycleTransition0To2()
        assertThat(viewModel.transition0To2.value).isEqualTo(TransitionState0To2.TWO_TO_ZERO)

        viewModel.cycleTransition0To2()
        assertThat(viewModel.transition0To2.value).isEqualTo(TransitionState0To2.ZERO_TWO_REPEAT)

        viewModel.cycleTransition0To2()
        assertThat(viewModel.transition0To2.value).isEqualTo(TransitionState0To2.DEFAULT)
    }

    @Test
    fun `cycleTransition1To2 should visit every state and persist it`() = runTest {
        val viewModel = createViewModel(this)

        viewModel.cycleTransition1To2()
        assertThat(viewModel.transition1To2.value).isEqualTo(TransitionState1To2.ONE_TO_TWO)
        assertThat(getTransitionState1To2(context)).isEqualTo(TransitionState1To2.ONE_TO_TWO)

        viewModel.cycleTransition1To2()
        assertThat(viewModel.transition1To2.value).isEqualTo(TransitionState1To2.TWO_TO_ONE)

        viewModel.cycleTransition1To2()
        assertThat(viewModel.transition1To2.value).isEqualTo(TransitionState1To2.ONE_TWO_REPEAT)

        viewModel.cycleTransition1To2()
        assertThat(viewModel.transition1To2.value).isEqualTo(TransitionState1To2.DEFAULT)
    }

    @Test
    fun `setting repeat on 1 to 2 should reset repeat on 0 to 2`() = runTest {
        val viewModel = createViewModel(this)

        viewModel.updateTransition0To2(TransitionState0To2.ZERO_TWO_REPEAT)
        viewModel.updateTransition1To2(TransitionState1To2.ONE_TWO_REPEAT)

        assertThat(viewModel.transition0To2.value).isEqualTo(TransitionState0To2.DEFAULT)
        assertThat(viewModel.transition1To2.value).isEqualTo(TransitionState1To2.ONE_TWO_REPEAT)
        assertThat(getTransitionState0To2(context)).isEqualTo(TransitionState0To2.DEFAULT)
        assertThat(getTransitionState1To2(context)).isEqualTo(TransitionState1To2.ONE_TWO_REPEAT)
    }

    @Test
    fun `setting repeat on 0 to 2 should reset repeat on 1 to 2`() = runTest {
        val viewModel = createViewModel(this)

        viewModel.updateTransition1To2(TransitionState1To2.ONE_TWO_REPEAT)
        viewModel.updateTransition0To2(TransitionState0To2.ZERO_TWO_REPEAT)

        assertThat(viewModel.transition0To2.value).isEqualTo(TransitionState0To2.ZERO_TWO_REPEAT)
        assertThat(viewModel.transition1To2.value).isEqualTo(TransitionState1To2.DEFAULT)
        assertThat(getTransitionState0To2(context)).isEqualTo(TransitionState0To2.ZERO_TWO_REPEAT)
        assertThat(getTransitionState1To2(context)).isEqualTo(TransitionState1To2.DEFAULT)
    }

    @Test
    fun `setting two to zero should reset existing two to one`() = runTest {
        val viewModel = createViewModel(this)

        viewModel.updateTransition1To2(TransitionState1To2.TWO_TO_ONE)
        viewModel.updateTransition0To2(TransitionState0To2.TWO_TO_ZERO)

        assertThat(viewModel.transition0To2.value).isEqualTo(TransitionState0To2.TWO_TO_ZERO)
        assertThat(viewModel.transition1To2.value).isEqualTo(TransitionState1To2.DEFAULT)
        assertThat(getTransitionState0To2(context)).isEqualTo(TransitionState0To2.TWO_TO_ZERO)
        assertThat(getTransitionState1To2(context)).isEqualTo(TransitionState1To2.DEFAULT)
    }

    @Test
    fun `setting two to one should reset existing two to zero`() = runTest {
        val viewModel = createViewModel(this)

        viewModel.updateTransition0To2(TransitionState0To2.TWO_TO_ZERO)
        viewModel.updateTransition1To2(TransitionState1To2.TWO_TO_ONE)

        assertThat(viewModel.transition0To2.value).isEqualTo(TransitionState0To2.DEFAULT)
        assertThat(viewModel.transition1To2.value).isEqualTo(TransitionState1To2.TWO_TO_ONE)
        assertThat(getTransitionState0To2(context)).isEqualTo(TransitionState0To2.DEFAULT)
        assertThat(getTransitionState1To2(context)).isEqualTo(TransitionState1To2.TWO_TO_ONE)
    }

    @Test
    fun `setting zero two repeat should reset existing two to one`() = runTest {
        val viewModel = createViewModel(this)

        viewModel.updateTransition1To2(TransitionState1To2.TWO_TO_ONE)
        viewModel.updateTransition0To2(TransitionState0To2.ZERO_TWO_REPEAT)

        assertThat(viewModel.transition0To2.value).isEqualTo(TransitionState0To2.ZERO_TWO_REPEAT)
        assertThat(viewModel.transition1To2.value).isEqualTo(TransitionState1To2.DEFAULT)
    }

    @Test
    fun `setting one two repeat should reset existing two to zero`() = runTest {
        val viewModel = createViewModel(this)

        viewModel.updateTransition0To2(TransitionState0To2.TWO_TO_ZERO)
        viewModel.updateTransition1To2(TransitionState1To2.ONE_TWO_REPEAT)

        assertThat(viewModel.transition0To2.value).isEqualTo(TransitionState0To2.DEFAULT)
        assertThat(viewModel.transition1To2.value).isEqualTo(TransitionState1To2.ONE_TWO_REPEAT)
    }

    @Test
    fun `cycling transition0 into two to zero should reset existing two to one`() = runTest {
        val viewModel = createViewModel(this)
        viewModel.updateTransition1To2(TransitionState1To2.TWO_TO_ONE)

        viewModel.cycleTransition0To2()
        viewModel.cycleTransition0To2()

        assertThat(viewModel.transition0To2.value).isEqualTo(TransitionState0To2.TWO_TO_ZERO)
        assertThat(viewModel.transition1To2.value).isEqualTo(TransitionState1To2.DEFAULT)
    }

    @Test
    fun `cycling transition1 into two to one should reset existing two to zero`() = runTest {
        val viewModel = createViewModel(this)
        viewModel.updateTransition0To2(TransitionState0To2.TWO_TO_ZERO)

        viewModel.cycleTransition1To2()
        viewModel.cycleTransition1To2()

        assertThat(viewModel.transition0To2.value).isEqualTo(TransitionState0To2.DEFAULT)
        assertThat(viewModel.transition1To2.value).isEqualTo(TransitionState1To2.TWO_TO_ONE)
    }

    @Test
    fun `cycling into repeat should still reset older conflicting button`() = runTest {
        val viewModel = createViewModel(this)
        viewModel.updateTransition0To2(TransitionState0To2.TWO_TO_ZERO)

        viewModel.cycleTransition1To2()
        viewModel.cycleTransition1To2()
        viewModel.cycleTransition1To2()

        assertThat(viewModel.transition0To2.value).isEqualTo(TransitionState0To2.DEFAULT)
        assertThat(viewModel.transition1To2.value).isEqualTo(TransitionState1To2.ONE_TWO_REPEAT)
    }

    @Test
    fun `inward only combination should remain valid`() = runTest {
        val viewModel = createViewModel(this)

        viewModel.updateTransition0To2(TransitionState0To2.ZERO_TO_TWO)
        viewModel.updateTransition1To2(TransitionState1To2.ONE_TO_TWO)

        assertThat(viewModel.transition0To2.value).isEqualTo(TransitionState0To2.ZERO_TO_TWO)
        assertThat(viewModel.transition1To2.value).isEqualTo(TransitionState1To2.ONE_TO_TWO)
    }

    @Test
    fun `one sided timer2 exit should remain until later conflicting tap`() = runTest {
        val viewModel = createViewModel(this)

        viewModel.updateTransition0To2(TransitionState0To2.TWO_TO_ZERO)
        viewModel.updateTransition1To2(TransitionState1To2.ONE_TO_TWO)

        assertThat(viewModel.transition0To2.value).isEqualTo(TransitionState0To2.TWO_TO_ZERO)
        assertThat(viewModel.transition1To2.value).isEqualTo(TransitionState1To2.ONE_TO_TWO)

        viewModel.updateTransition1To2(TransitionState1To2.TWO_TO_ONE)

        assertThat(viewModel.transition0To2.value).isEqualTo(TransitionState0To2.DEFAULT)
        assertThat(viewModel.transition1To2.value).isEqualTo(TransitionState1To2.TWO_TO_ONE)
    }

    @Test
    fun `completed non repeating timer should stop`() = runTest {
        val clock = MutableClock(now = 0L)
        val serviceController = RecordingTimerServiceController()
        val viewModel = createViewModel(this, clock, serviceController)

        configureTimer(
            viewModel = viewModel,
            timerId = "timer0",
            durationMillis = 2_000L,
            repeating = false,
        )
        viewModel.startTimer("timer0", context, backgroundScope)

        clock.now = 2_000L
        advanceTimeBy(2_000L)
        runCurrent()

        assertThat(viewModel.timer0.value.started).isFalse()
        assertThat(viewModel.timer0.value.timeRemaining).isLessThanOrEqualTo(0L)
        assertThat(serviceController.startRequests).hasSize(1)
        assertThat(serviceController.stopCalls).isEqualTo(2)
        assertThat(getActiveTimerState(context)).isNull()
    }

    @Test
    fun `completed repeating timer should restart with saved duration`() = runTest {
        val clock = MutableClock(now = 0L)
        val serviceController = RecordingTimerServiceController()
        val viewModel = createViewModel(this, clock, serviceController)

        configureTimer(
            viewModel = viewModel,
            timerId = "timer2",
            durationMillis = 2_000L,
            repeating = true,
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
        assertThat(serviceController.startRequests.last().suppressStartAlert).isFalse()
    }

    @Test
    fun `timer0 completion should start timer2 for zero to two`() = runTest {
        val clock = MutableClock(now = 0L)
        val serviceController = RecordingTimerServiceController()
        val viewModel = createViewModel(this, clock, serviceController)
        viewModel.updateTransition0To2(TransitionState0To2.ZERO_TO_TWO)
        configureTimer(viewModel, "timer0", 2_000L, repeating = false)

        viewModel.startTimer("timer0", context, backgroundScope)

        clock.now = 2_000L
        advanceTimeBy(2_000L)
        runCurrent()

        assertActiveTimer(viewModel, "timer2")
        assertThat(serviceController.startRequests).hasSize(2)
        assertThat(serviceController.startRequests.last().durationMillis).isEqualTo(viewModel.timer2.value.durationMillis)
        assertThat(serviceController.startRequests.last().suppressStartAlert).isTrue()
        assertThat(getActiveTimerState(context)?.timerId).isEqualTo("timer2")
    }

    @Test
    fun `timer2 completion should start timer0 for two to zero`() = runTest {
        val clock = MutableClock(now = 0L)
        val serviceController = RecordingTimerServiceController()
        val viewModel = createViewModel(this, clock, serviceController)
        viewModel.updateTransition0To2(TransitionState0To2.TWO_TO_ZERO)
        configureTimer(viewModel, "timer2", 2_000L, repeating = false)

        viewModel.startTimer("timer2", context, backgroundScope)

        clock.now = 2_000L
        advanceTimeBy(2_000L)
        runCurrent()

        assertActiveTimer(viewModel, "timer0")
        assertThat(serviceController.startRequests.last().suppressStartAlert).isTrue()
    }

    @Test
    fun `zero two repeat should alternate between timer0 and timer2`() = runTest {
        val clock = MutableClock(now = 0L)
        val serviceController = RecordingTimerServiceController()
        val viewModel = createViewModel(this, clock, serviceController)
        viewModel.updateTransition0To2(TransitionState0To2.ZERO_TWO_REPEAT)
        configureTimer(viewModel, "timer0", 2_000L, repeating = false)
        configureTimer(viewModel, "timer2", 2_000L, repeating = false)

        viewModel.startTimer("timer0", context, backgroundScope)
        clock.now = 2_000L
        advanceTimeBy(2_000L)
        runCurrent()
        assertActiveTimer(viewModel, "timer2")

        clock.now = 4_000L
        advanceTimeBy(2_000L)
        runCurrent()
        assertActiveTimer(viewModel, "timer0")
    }

    @Test
    fun `one two repeat should alternate between timer1 and timer2`() = runTest {
        val clock = MutableClock(now = 0L)
        val serviceController = RecordingTimerServiceController()
        val viewModel = createViewModel(this, clock, serviceController)
        viewModel.updateTransition1To2(TransitionState1To2.ONE_TWO_REPEAT)
        configureTimer(viewModel, "timer1", 2_000L, repeating = false)
        configureTimer(viewModel, "timer2", 2_000L, repeating = false)

        viewModel.startTimer("timer1", context, backgroundScope)
        clock.now = 2_000L
        advanceTimeBy(2_000L)
        runCurrent()
        assertActiveTimer(viewModel, "timer2")

        clock.now = 4_000L
        advanceTimeBy(2_000L)
        runCurrent()
        assertActiveTimer(viewModel, "timer1")
    }

    @Test
    fun `timer1 completion should start timer2 for one to two`() = runTest {
        val clock = MutableClock(now = 0L)
        val serviceController = RecordingTimerServiceController()
        val viewModel = createViewModel(this, clock, serviceController)
        viewModel.updateTransition1To2(TransitionState1To2.ONE_TO_TWO)
        configureTimer(viewModel, "timer1", 2_000L, repeating = false)

        viewModel.startTimer("timer1", context, backgroundScope)
        clock.now = 2_000L
        advanceTimeBy(2_000L)
        runCurrent()

        assertActiveTimer(viewModel, "timer2")
    }

    @Test
    fun `timer2 completion should start timer1 for two to one`() = runTest {
        val clock = MutableClock(now = 0L)
        val serviceController = RecordingTimerServiceController()
        val viewModel = createViewModel(this, clock, serviceController)
        viewModel.updateTransition1To2(TransitionState1To2.TWO_TO_ONE)
        configureTimer(viewModel, "timer2", 2_000L, repeating = false)

        viewModel.startTimer("timer2", context, backgroundScope)
        clock.now = 2_000L
        advanceTimeBy(2_000L)
        runCurrent()

        assertActiveTimer(viewModel, "timer1")
    }

    @Test
    fun `timer2 should choose timer0 when only zero to two rule points away from it`() = runTest {
        val clock = MutableClock(now = 0L)
        val serviceController = RecordingTimerServiceController()
        val viewModel = createViewModel(this, clock, serviceController)
        viewModel.updateTransition0To2(TransitionState0To2.TWO_TO_ZERO)
        viewModel.updateTransition1To2(TransitionState1To2.ONE_TO_TWO)
        configureTimer(viewModel, "timer2", 2_000L, repeating = false)

        viewModel.startTimer("timer2", context, backgroundScope)
        clock.now = 2_000L
        advanceTimeBy(2_000L)
        runCurrent()

        assertActiveTimer(viewModel, "timer0")
    }

    @Test
    fun `transition should take precedence over self repeat`() = runTest {
        val clock = MutableClock(now = 0L)
        val serviceController = RecordingTimerServiceController()
        val viewModel = createViewModel(this, clock, serviceController)
        viewModel.updateTransition0To2(TransitionState0To2.ZERO_TO_TWO)
        configureTimer(viewModel, "timer0", 2_000L, repeating = true)

        viewModel.startTimer("timer0", context, backgroundScope)
        clock.now = 2_000L
        advanceTimeBy(2_000L)
        runCurrent()

        assertActiveTimer(viewModel, "timer2")
        assertThat(viewModel.timer0.value.started).isFalse()
        assertThat(serviceController.startRequests.last().suppressStartAlert).isTrue()
    }

    @Test
    fun `self repeat should still work when no outgoing transition applies`() = runTest {
        val clock = MutableClock(now = 0L)
        val serviceController = RecordingTimerServiceController()
        val viewModel = createViewModel(this, clock, serviceController)
        configureTimer(viewModel, "timer1", 2_000L, repeating = true)

        viewModel.startTimer("timer1", context, backgroundScope)
        clock.now = 2_000L
        advanceTimeBy(2_000L)
        runCurrent()

        assertActiveTimer(viewModel, "timer1")
        assertThat(serviceController.startRequests.last().suppressStartAlert).isFalse()
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

    private fun configureTimer(
        viewModel: TimerViewModel,
        timerId: String,
        durationMillis: Long,
        repeating: Boolean,
    ) {
        viewModel.updateTimerDuration(
            id = timerId,
            newDurationMillis = durationMillis,
            newRepeating = repeating,
            newSound = false,
            newVibration = true,
            newIntervalStuff = true,
        )
    }

    private fun assertActiveTimer(viewModel: TimerViewModel, activeTimerId: String) {
        assertThat(viewModel.timer0.value.started).isEqualTo(activeTimerId == "timer0")
        assertThat(viewModel.timer1.value.started).isEqualTo(activeTimerId == "timer1")
        assertThat(viewModel.timer2.value.started).isEqualTo(activeTimerId == "timer2")
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
