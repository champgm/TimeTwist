package com.cgm.timetwist.presentation

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.Job
import androidx.lifecycle.viewModelScope
import com.cgm.timetwist.service.CountdownService
import com.cgm.timetwist.service.TimeDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.EmptyCoroutineContext

private const val TIME_TWIST_PREFERENCES = "time_twist_preferences"

fun saveTimerDetails(context: Context, timerId: String, details: TimeDetails) {
    val prefs = context.getSharedPreferences(TIME_TWIST_PREFERENCES, Context.MODE_PRIVATE)
    with(prefs.edit()) {
        putLong("${timerId}_durationMillis", details.durationMillis)
        putBoolean("${timerId}_repeating", details.repeating)
        apply()
    }
}

fun getTimerDetails(context: Context, timerId: String): TimeDetails? {
    val prefs = context.getSharedPreferences(TIME_TWIST_PREFERENCES, Context.MODE_PRIVATE)
    val durationMillis = prefs.getLong("${timerId}_durationMillis", -1L)
    val repeating = prefs.getBoolean("${timerId}_repeating", false)
    if (durationMillis != -1L) {
        return TimeDetails(timerId, durationMillis = durationMillis, repeating = repeating)
    }
    return null
}


class TimerViewModel(application: Application) : AndroidViewModel(application) {
    var timer0: MutableState<TimeDetails> = mutableStateOf(TimeDetails("timer0", durationMillis = 33000L, repeating = false))
    var timer1: MutableState<TimeDetails> = mutableStateOf(TimeDetails("timer1", durationMillis = 5000L, repeating = true))
    var timer2: MutableState<TimeDetails> = mutableStateOf(TimeDetails("timer2", durationMillis = 310000L))
    private var cachedCoroutineScope: CoroutineScope = CoroutineScope(EmptyCoroutineContext)
    private var timerJob: Job? = null

    init {
        startTimers()
        loadTimersFromPrefs()
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
        timerJob = viewModelScope.launch {
            while (true) {
                listOf(timer0, timer1, timer2).forEach { timer ->
                    if (timer.value.started) {
                        updateTimer(timer.value.timerId)
                        if (timer.value.timeRemaining <= 0L) {
                            val context = getApplication<Application>().applicationContext
                            stopService(context, cachedCoroutineScope)
                            stopTimer(timer.value.timerId)
                            if (timer.value.repeating) {
                                startService(
                                    context, cachedCoroutineScope,
                                    timer.value.durationMillis,
                                    timer.value.repeating
                                )
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
        val intent = Intent(context, CountdownService::class.java)
        context.stopService(intent)
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
        val currentTime = System.currentTimeMillis()
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
        timer.value = timer.value.copy(
            started = false
        )
    }

    fun startTimer(startTimerId: String, context: Context, coroutineScope: CoroutineScope) {
        cachedCoroutineScope = coroutineScope
        stopService(context, coroutineScope)
        listOf(timer0, timer1, timer2).forEach { timer ->
            if (timer.value.timerId == startTimerId) {
                timer.value = timer.value.copy(
                    startTime = System.currentTimeMillis(),
                    started = true
                )
                startService(context, coroutineScope, timer.value.durationMillis, timer.value.repeating)
                updateTimer(timer.value.timerId)
            } else {
                stopTimer(timer.value.timerId)
            }
        }
    }

    fun updateTimerDuration(id: String, newDurationMillis: Long, newRepeating: Boolean) {
        when (id) {
            "timer0" -> timer0.value = timer0.value.copy(durationMillis = newDurationMillis, repeating = newRepeating)
            "timer1" -> timer1.value = timer1.value.copy(durationMillis = newDurationMillis, repeating = newRepeating)
            "timer2" -> timer2.value = timer2.value.copy(durationMillis = newDurationMillis, repeating = newRepeating)
            else -> throw IllegalArgumentException("Invalid timerId")
        }
        val context = getApplication<Application>().applicationContext
        saveTimerDetails(context, id, TimeDetails(id, durationMillis = newDurationMillis, repeating = newRepeating))
    }

    private fun startService(
        context: Context,
        coroutineScope: CoroutineScope,
        durationMillis: Long,
        repeating: Boolean
    ) {
        cachedCoroutineScope = coroutineScope
        val startTime = System.currentTimeMillis()
        coroutineScope.launch {
            val intent = Intent(context, CountdownService::class.java)
            intent.putExtra("startTime", startTime)
            intent.putExtra("durationMillis", durationMillis)
            intent.putExtra("repeating", repeating)
            context.startService(intent)
        }
    }

    private fun stopService(context: Context, coroutineScope: CoroutineScope) {
        cachedCoroutineScope = coroutineScope
        val intent = Intent(context, CountdownService::class.java)
        context.stopService(intent)
    }

}