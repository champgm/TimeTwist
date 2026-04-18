package com.cgm.timetwist.presentation

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cgm.timetwist.service.CountdownService
import com.cgm.timetwist.service.TimeDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.EmptyCoroutineContext

private const val TIME_TWIST_PREFERENCES = "time_twist_preferences"
private const val TRANSITION_0_2_KEY = "transition_0_2"
private const val TRANSITION_1_2_KEY = "transition_1_2"

enum class TransitionState0To2 {
    DEFAULT,
    ZERO_TO_TWO,
    TWO_TO_ZERO,
    ZERO_TWO_REPEAT,
}

enum class TransitionState1To2 {
    DEFAULT,
    ONE_TO_TWO,
    TWO_TO_ONE,
    ONE_TWO_REPEAT,
}

private enum class TransitionUpdateSource {
    TRANSITION_0_2,
    TRANSITION_1_2,
    STARTUP_REPAIR,
}

internal data class TimerServiceRequest(
    val startTime: Long,
    val durationMillis: Long,
    val repeating: Boolean,
    val sound: Boolean,
    val vibration: Boolean,
    val intervalStuff: Boolean,
    val suppressStartAlert: Boolean,
)

internal interface TimerServiceController {
    fun start(context: Context, coroutineScope: CoroutineScope, request: TimerServiceRequest)
    fun stop(context: Context, coroutineScope: CoroutineScope)
}

internal object AndroidTimerServiceController : TimerServiceController {
    override fun start(
        context: Context,
        coroutineScope: CoroutineScope,
        request: TimerServiceRequest,
    ) {
        coroutineScope.launch {
            val intent = Intent(context, CountdownService::class.java)
            intent.putExtra("startTime", request.startTime)
            intent.putExtra("durationMillis", request.durationMillis)
            intent.putExtra("repeating", request.repeating)
            intent.putExtra("sound", request.sound)
            intent.putExtra("vibration", request.vibration)
            intent.putExtra("intervalStuff", request.intervalStuff)
            intent.putExtra("suppressStartAlert", request.suppressStartAlert)
            context.startService(intent)
        }
    }

    override fun stop(context: Context, coroutineScope: CoroutineScope) {
        context.stopService(Intent(context, CountdownService::class.java))
    }
}

fun saveTimerDetails(context: Context, timerId: String, details: TimeDetails) {
    val prefs = context.getSharedPreferences(TIME_TWIST_PREFERENCES, Context.MODE_PRIVATE)
    with(prefs.edit()) {
        putLong("${timerId}_durationMillis", details.durationMillis)
        putBoolean("${timerId}_repeating", details.repeating)
        putBoolean("${timerId}_vibration", details.vibration)
        putBoolean("${timerId}_sound", details.sound)
        putBoolean("${timerId}_intervalStuff", details.intervalStuff)
        apply()
    }
}

fun getTimerDetails(context: Context, timerId: String): TimeDetails? {
    val prefs = context.getSharedPreferences(TIME_TWIST_PREFERENCES, Context.MODE_PRIVATE)
    val durationMillis = prefs.getLong("${timerId}_durationMillis", -1L)
    val repeating = prefs.getBoolean("${timerId}_repeating", false)
    val vibration = prefs.getBoolean("${timerId}_vibration", false)
    val sound = prefs.getBoolean("${timerId}_sound", false)
    val intervalStuff = prefs.getBoolean("${timerId}_intervalStuff", false)
    return if (durationMillis != -1L) {
        TimeDetails(
            timerId = timerId,
            durationMillis = durationMillis,
            repeating = repeating,
            vibration = vibration,
            sound = sound,
            intervalStuff = intervalStuff
        )
    } else {
        null
    }
}

fun saveTransitionState0To2(context: Context, state: TransitionState0To2) {
    val prefs = context.getSharedPreferences(TIME_TWIST_PREFERENCES, Context.MODE_PRIVATE)
    prefs.edit().putString(TRANSITION_0_2_KEY, state.name).apply()
}

fun getTransitionState0To2(context: Context): TransitionState0To2 {
    val prefs = context.getSharedPreferences(TIME_TWIST_PREFERENCES, Context.MODE_PRIVATE)
    val savedValue = prefs.getString(TRANSITION_0_2_KEY, null) ?: return TransitionState0To2.DEFAULT
    return TransitionState0To2.entries.firstOrNull { it.name == savedValue }
        ?: TransitionState0To2.DEFAULT
}

fun saveTransitionState1To2(context: Context, state: TransitionState1To2) {
    val prefs = context.getSharedPreferences(TIME_TWIST_PREFERENCES, Context.MODE_PRIVATE)
    prefs.edit().putString(TRANSITION_1_2_KEY, state.name).apply()
}

fun getTransitionState1To2(context: Context): TransitionState1To2 {
    val prefs = context.getSharedPreferences(TIME_TWIST_PREFERENCES, Context.MODE_PRIVATE)
    val savedValue = prefs.getString(TRANSITION_1_2_KEY, null) ?: return TransitionState1To2.DEFAULT
    return TransitionState1To2.entries.firstOrNull { it.name == savedValue }
        ?: TransitionState1To2.DEFAULT
}

class TimerViewModel internal constructor(
    application: Application,
    private val timeProvider: () -> Long,
    private val timerServiceController: TimerServiceController,
    private val timerCoroutineScope: CoroutineScope?,
) : AndroidViewModel(application) {
    constructor(application: Application) : this(
        application = application,
        timeProvider = System::currentTimeMillis,
        timerServiceController = AndroidTimerServiceController,
        timerCoroutineScope = null,
    )

    var timer0: MutableState<TimeDetails> =
        mutableStateOf(
            TimeDetails(
                timerId = "timer0",
                durationMillis = 33000L,
                repeating = false,
                sound = false,
                vibration = true,
                intervalStuff = true,
            )
        )
    var timer1: MutableState<TimeDetails> =
        mutableStateOf(
            TimeDetails(
                timerId = "timer1",
                durationMillis = 5000L,
                repeating = false,
                sound = true,
                vibration = true,
                intervalStuff = true,
            )
        )
    var timer2: MutableState<TimeDetails> =
        mutableStateOf(
            TimeDetails(
                timerId = "timer2",
                durationMillis = 310000L,
                repeating = false,
                sound = true,
                vibration = true,
                intervalStuff = true,
            )
        )
    var transition0To2: MutableState<TransitionState0To2> =
        mutableStateOf(TransitionState0To2.DEFAULT)
    var transition1To2: MutableState<TransitionState1To2> =
        mutableStateOf(TransitionState1To2.DEFAULT)

    private var cachedCoroutineScope: CoroutineScope = CoroutineScope(EmptyCoroutineContext)
    private var timerJob: Job? = null

    init {
        loadTimersFromPrefs()
        startTimers()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    private fun loadTimersFromPrefs() {
        val context = getApplication<Application>().applicationContext
        listOf("timer0", "timer1", "timer2").forEach { timerId ->
            getTimerDetails(context, timerId)?.let { details ->
                when (timerId) {
                    "timer0" -> timer0.value = details
                    "timer1" -> timer1.value = details
                    "timer2" -> timer2.value = details
                }
            }
        }
        val normalizedTransitions = normalizeTransitionStates(
            state0To2 = getTransitionState0To2(context),
            state1To2 = getTransitionState1To2(context),
            source = TransitionUpdateSource.STARTUP_REPAIR,
        )
        transition0To2.value = normalizedTransitions.first
        transition1To2.value = normalizedTransitions.second
        saveTransitionState0To2(context, transition0To2.value)
        saveTransitionState1To2(context, transition1To2.value)
    }

    private fun startTimers() {
        timerJob = (timerCoroutineScope ?: viewModelScope).launch {
            while (true) {
                listOf(timer0, timer1, timer2).forEach { timer ->
                    if (timer.value.started) {
                        updateTimer(timer.value.timerId)
                        if (timer.value.timeRemaining <= 0L) {
                            val context = getApplication<Application>().applicationContext
                            val completedTimerId = timer.value.timerId
                            val shouldSelfRepeat = timer.value.repeating
                            stopService(context, cachedCoroutineScope)
                            stopTimer(completedTimerId)
                            val nextTimerId = resolveNextTimerId(completedTimerId)
                            when {
                                nextTimerId != null -> startTimer(
                                    startTimerId = nextTimerId,
                                    context = context,
                                    coroutineScope = cachedCoroutineScope,
                                    suppressStartAlert = true,
                                )
                                shouldSelfRepeat -> startTimer(
                                    startTimerId = completedTimerId,
                                    context = context,
                                    coroutineScope = cachedCoroutineScope,
                                    suppressStartAlert = false,
                                )
                            }
                        }
                    }
                }
                delay(1000L)
            }
        }
    }

    fun stopTimers(context: Context) {
        stopService(context, cachedCoroutineScope)
        listOf(timer0, timer1, timer2).forEach { timer ->
            if (timer.value.started) {
                stopTimer(timer.value.timerId)
            }
        }
    }

    fun getTimer(timerId: String): TimeDetails {
        return when (timerId) {
            "timer0" -> timer0.value
            "timer1" -> timer1.value
            "timer2" -> timer2.value
            else -> throw IllegalArgumentException("Invalid timerId")
        }
    }

    private fun updateTimer(timerId: String) {
        val timer = when (timerId) {
            "timer0" -> timer0
            "timer1" -> timer1
            "timer2" -> timer2
            else -> throw IllegalArgumentException("Invalid timerId")
        }
        val currentTime = timeProvider()
        val elapsedTime = currentTime - timer.value.startTime
        val timeRemaining = timer.value.durationMillis - elapsedTime

        timer.value = timer.value.copy(
            elapsedTime = elapsedTime,
            timeRemaining = timeRemaining,
        )
    }

    private fun stopTimer(timerId: String) {
        val timer = when (timerId) {
            "timer0" -> timer0
            "timer1" -> timer1
            "timer2" -> timer2
            else -> throw IllegalArgumentException("Invalid timerId")
        }
        timer.value = timer.value.copy(started = false)
    }

    fun startTimer(startTimerId: String, context: Context, coroutineScope: CoroutineScope) {
        startTimer(
            startTimerId = startTimerId,
            context = context,
            coroutineScope = coroutineScope,
            suppressStartAlert = false,
        )
    }

    fun cycleTransition0To2() {
        val nextState = when (transition0To2.value) {
            TransitionState0To2.DEFAULT -> TransitionState0To2.ZERO_TO_TWO
            TransitionState0To2.ZERO_TO_TWO -> TransitionState0To2.TWO_TO_ZERO
            TransitionState0To2.TWO_TO_ZERO -> TransitionState0To2.ZERO_TWO_REPEAT
            TransitionState0To2.ZERO_TWO_REPEAT -> TransitionState0To2.DEFAULT
        }
        updateTransition0To2(nextState)
    }

    fun cycleTransition1To2() {
        val nextState = when (transition1To2.value) {
            TransitionState1To2.DEFAULT -> TransitionState1To2.ONE_TO_TWO
            TransitionState1To2.ONE_TO_TWO -> TransitionState1To2.TWO_TO_ONE
            TransitionState1To2.TWO_TO_ONE -> TransitionState1To2.ONE_TWO_REPEAT
            TransitionState1To2.ONE_TWO_REPEAT -> TransitionState1To2.DEFAULT
        }
        updateTransition1To2(nextState)
    }

    internal fun updateTransition0To2(newState: TransitionState0To2) {
        val context = getApplication<Application>().applicationContext
        val normalizedTransitions = normalizeTransitionStates(
            state0To2 = newState,
            state1To2 = transition1To2.value,
            source = TransitionUpdateSource.TRANSITION_0_2,
        )
        transition0To2.value = normalizedTransitions.first
        transition1To2.value = normalizedTransitions.second
        saveTransitionState0To2(context, transition0To2.value)
        saveTransitionState1To2(context, transition1To2.value)
    }

    internal fun updateTransition1To2(newState: TransitionState1To2) {
        val context = getApplication<Application>().applicationContext
        val normalizedTransitions = normalizeTransitionStates(
            state0To2 = transition0To2.value,
            state1To2 = newState,
            source = TransitionUpdateSource.TRANSITION_1_2,
        )
        transition0To2.value = normalizedTransitions.first
        transition1To2.value = normalizedTransitions.second
        saveTransitionState0To2(context, transition0To2.value)
        saveTransitionState1To2(context, transition1To2.value)
    }

    private fun pointsAwayFromTimer2(state: TransitionState0To2): Boolean {
        return state == TransitionState0To2.TWO_TO_ZERO ||
            state == TransitionState0To2.ZERO_TWO_REPEAT
    }

    private fun pointsAwayFromTimer2(state: TransitionState1To2): Boolean {
        return state == TransitionState1To2.TWO_TO_ONE ||
            state == TransitionState1To2.ONE_TWO_REPEAT
    }

    private fun normalizeTransitionStates(
        state0To2: TransitionState0To2,
        state1To2: TransitionState1To2,
        source: TransitionUpdateSource,
    ): Pair<TransitionState0To2, TransitionState1To2> {
        val repeatConflict =
            state0To2 == TransitionState0To2.ZERO_TWO_REPEAT &&
                state1To2 == TransitionState1To2.ONE_TWO_REPEAT
        val dualExitConflict =
            pointsAwayFromTimer2(state0To2) &&
                pointsAwayFromTimer2(state1To2)

        if (!repeatConflict && !dualExitConflict) {
            return state0To2 to state1To2
        }

        return when (source) {
            TransitionUpdateSource.TRANSITION_0_2 -> state0To2 to TransitionState1To2.DEFAULT
            TransitionUpdateSource.TRANSITION_1_2 -> TransitionState0To2.DEFAULT to state1To2
            TransitionUpdateSource.STARTUP_REPAIR ->
                TransitionState0To2.DEFAULT to TransitionState1To2.DEFAULT
        }
    }

    private fun startTimer(
        startTimerId: String,
        context: Context,
        coroutineScope: CoroutineScope,
        suppressStartAlert: Boolean,
    ) {
        cachedCoroutineScope = coroutineScope
        stopService(context, coroutineScope)
        listOf(timer0, timer1, timer2).forEach { timer ->
            if (timer.value.timerId == startTimerId) {
                val startTime = timeProvider()
                timer.value = timer.value.copy(
                    startTime = startTime,
                    started = true
                )
                startService(
                    context = context,
                    coroutineScope = coroutineScope,
                    startTime = startTime,
                    durationMillis = timer.value.durationMillis,
                    repeating = timer.value.repeating,
                    sound = timer.value.sound,
                    vibration = timer.value.vibration,
                    intervalStuff = timer.value.intervalStuff,
                    suppressStartAlert = suppressStartAlert,
                )
                updateTimer(timer.value.timerId)
            } else {
                stopTimer(timer.value.timerId)
            }
        }
    }

    fun updateTimerDuration(
        id: String,
        newDurationMillis: Long,
        newRepeating: Boolean,
        newSound: Boolean,
        newVibration: Boolean,
        newIntervalStuff: Boolean,
    ) {
        when (id) {
            "timer0" -> timer0.value =
                timer0.value.copy(
                    durationMillis = newDurationMillis,
                    repeating = newRepeating,
                    sound = newSound,
                    vibration = newVibration,
                    intervalStuff = newIntervalStuff,
                )

            "timer1" -> timer1.value =
                timer1.value.copy(
                    durationMillis = newDurationMillis,
                    repeating = newRepeating,
                    sound = newSound,
                    vibration = newVibration,
                    intervalStuff = newIntervalStuff,
                )

            "timer2" -> timer2.value =
                timer2.value.copy(
                    durationMillis = newDurationMillis,
                    repeating = newRepeating,
                    sound = newSound,
                    vibration = newVibration,
                    intervalStuff = newIntervalStuff,
                )

            else -> throw IllegalArgumentException("Invalid timerId")
        }
        val context = getApplication<Application>().applicationContext
        saveTimerDetails(
            context = context,
            timerId = id,
            details = TimeDetails(
                timerId = id,
                durationMillis = newDurationMillis,
                repeating = newRepeating,
                sound = newSound,
                vibration = newVibration,
                intervalStuff = newIntervalStuff,
            )
        )
    }

    private fun startService(
        context: Context,
        coroutineScope: CoroutineScope,
        startTime: Long,
        durationMillis: Long,
        repeating: Boolean,
        sound: Boolean,
        vibration: Boolean,
        intervalStuff: Boolean,
        suppressStartAlert: Boolean,
    ) {
        cachedCoroutineScope = coroutineScope
        timerServiceController.start(
            context = context,
            coroutineScope = coroutineScope,
            request = TimerServiceRequest(
                startTime = startTime,
                durationMillis = durationMillis,
                repeating = repeating,
                sound = sound,
                vibration = vibration,
                intervalStuff = intervalStuff,
                suppressStartAlert = suppressStartAlert,
            )
        )
    }

    private fun resolveNextTimerId(completedTimerId: String): String? {
        return when (completedTimerId) {
            "timer0" -> when (transition0To2.value) {
                TransitionState0To2.ZERO_TO_TWO,
                TransitionState0To2.ZERO_TWO_REPEAT -> "timer2"
                TransitionState0To2.DEFAULT,
                TransitionState0To2.TWO_TO_ZERO -> null
            }

            "timer1" -> when (transition1To2.value) {
                TransitionState1To2.ONE_TO_TWO,
                TransitionState1To2.ONE_TWO_REPEAT -> "timer2"
                TransitionState1To2.DEFAULT,
                TransitionState1To2.TWO_TO_ONE -> null
            }

            "timer2" -> resolveTimer2Transition()
            else -> throw IllegalArgumentException("Invalid timerId")
        }
    }

    private fun resolveTimer2Transition(): String? {
        val routeToTimer0 =
            transition0To2.value == TransitionState0To2.TWO_TO_ZERO ||
                transition0To2.value == TransitionState0To2.ZERO_TWO_REPEAT
        val routeToTimer1 =
            transition1To2.value == TransitionState1To2.TWO_TO_ONE ||
                transition1To2.value == TransitionState1To2.ONE_TWO_REPEAT

        return when {
            routeToTimer0 && routeToTimer1 -> null
            routeToTimer0 -> "timer0"
            routeToTimer1 -> "timer1"
            else -> null
        }
    }

    private fun stopService(context: Context, coroutineScope: CoroutineScope) {
        cachedCoroutineScope = coroutineScope
        timerServiceController.stop(context, coroutineScope)
    }
}
