package com.example.timetwist.presentation

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Job
import androidx.lifecycle.viewModelScope
import com.example.timetwist.service.CountdownService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TimerViewModel : ViewModel() {
    // MutableState for timer0, timer1, and timer2
    var timer0: MutableState<TimeDetails> = mutableStateOf(TimeDetails(durationMillis = 5000L, repeating = true))
    var timer1: MutableState<TimeDetails> = mutableStateOf(TimeDetails(durationMillis = 315000L))
    var timer2: MutableState<TimeDetails> = mutableStateOf(TimeDetails(durationMillis = 60000L))

    private var timerJob: Job? = null

    init {
        startTimers()
    }

    private fun startTimers() {
        timerJob = viewModelScope.launch {
            while (true) {
                listOf(timer0, timer1, timer2).forEach { timer ->
                    if (timer.value.started) {
                        updateTimer(timer)
                        if (timer.value.timeRemaining <= 0L) {
                            stopTimer(timer)
                            if (timer.value.repeating) {
                                toggleTimer(timer)
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
                stopTimer(timer)
            }
        }
    }

    // Functions to modify timers
    private fun updateTimer(timer: MutableState<TimeDetails>) {
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - timer.value.startTime
        val timeRemaining = timer.value.durationMillis - elapsedTime

        timer.value = timer.value.copy(
            elapsedTime = elapsedTime,
            timeRemaining = timeRemaining,
        )
    }

    private fun stopTimer(timer: MutableState<TimeDetails>) {
        timer.value = timer.value.copy(
            started = false
        )
    }

    fun stopTimer(timerId: String) {
        stopTimer(
            when (timerId) {
                "timer0" -> timer0
                "timer1" -> timer1
                "timer2" -> timer2
                else -> throw IllegalArgumentException("Invalid timerId")
            }
        )
    }

    fun toggleTimer(timer: MutableState<TimeDetails>) {
        timer.value = timer.value.copy(
            startTime = System.currentTimeMillis(),
            started = !timer.value.started
        )
        if (timer.value.started) {
            updateTimer(timer)
        }
    }

    fun updateTimerDuration(id: String, newDurationMillis: Long, newRepeating: Boolean) {
        Log.e("viewmodel", "Updating timer with id: ${id}")
        when (id) {
            "timer0" -> timer0.value = timer0.value.copy(durationMillis = newDurationMillis, repeating = newRepeating)
            "timer1" -> timer1.value = timer1.value.copy(durationMillis = newDurationMillis, repeating = newRepeating)
            "timer2" -> timer2.value = timer2.value.copy(durationMillis = newDurationMillis, repeating = newRepeating)
            else -> throw IllegalArgumentException("Invalid timerId")
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    fun startTimerStopOthers(startId: String) {
        val timers = listOf("timer0", "timer1", "timer2")
        timers.forEach { timerId ->
            if (startId == timerId) {
                toggleTimer(
                    when (timerId) {
                        "timer0" -> timer0
                        "timer1" -> timer1
                        "timer2" -> timer2
                        else -> throw IllegalArgumentException("Invalid timerId")
                    }
                )
            } else {
                stopTimer(
                    when (timerId) {
                        "timer0" -> timer0
                        "timer1" -> timer1
                        "timer2" -> timer2
                        else -> throw IllegalArgumentException("Invalid timerId")
                    }
                )
            }
        }
    }

    private fun startService(context: Context, coroutineScope: CoroutineScope, durationMillis: Long) {
        val startTime = System.currentTimeMillis()
        coroutineScope.launch {
            val intent = Intent(context, CountdownService::class.java)
            intent.putExtra("startTime", startTime)
            intent.putExtra("durationMillis", durationMillis)
            context.startService(intent)
        }
    }

    fun toggleService(context: Context, coroutineScope: CoroutineScope) {
        val intent = Intent(context, CountdownService::class.java)
        context.stopService(intent)

        if (timer0.value.started) {
            startService(context, coroutineScope, timer0.value.durationMillis)
        } else if (timer1.value.started) {
            startService(context, coroutineScope, timer1.value.durationMillis)
        } else if (timer2.value.started) {
            startService(context, coroutineScope, timer2.value.durationMillis)
        }
    }
}