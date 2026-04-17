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

internal data class TimerServiceRequest(
    val startTime: Long,
    val durationMillis: Long,
    val repeating: Boolean,
    val sound: Boolean,
    val vibration: Boolean,
    val intervalStuff: Boolean,
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
    }

    private fun startTimers() {
        timerJob = (timerCoroutineScope ?: viewModelScope).launch {
            while (true) {
                listOf(timer0, timer1, timer2).forEach { timer ->
                    if (timer.value.started) {
                        updateTimer(timer.value.timerId)
                        if (timer.value.timeRemaining <= 0L) {
                            val context = getApplication<Application>().applicationContext
                            stopService(context, cachedCoroutineScope)
                            stopTimer(timer.value.timerId)
                            if (timer.value.repeating) {
                                startTimer(timer.value.timerId, context, cachedCoroutineScope)
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
            )
        )
    }

    private fun stopService(context: Context, coroutineScope: CoroutineScope) {
        cachedCoroutineScope = coroutineScope
        timerServiceController.stop(context, coroutineScope)
    }
}
