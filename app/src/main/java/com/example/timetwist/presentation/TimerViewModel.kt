package com.example.timetwist.presentation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class TimerViewModel : ViewModel() {
    // MutableState for timer0, timer1, and timer2
    var timer0: MutableState<TimeDetails> = mutableStateOf(TimeDetails(30000L))
    var timer1: MutableState<TimeDetails> = mutableStateOf(TimeDetails(300000L))
    var timer2: MutableState<TimeDetails> = mutableStateOf(TimeDetails(60000L))

    // Functions to modify timers
    fun updateTimer(timer: MutableState<TimeDetails>) {
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - timer.value.startTime
        val timeRemaining = timer.value.durationMillis - elapsedTime

        timer.value = timer.value.copy(
            elapsedTime = elapsedTime,
            timeRemaining = timeRemaining,
        )
    }

    fun stopTimer(timer: MutableState<TimeDetails>) {
        timer.value = timer.value.copy(
            started = false
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

    fun updateTimerDuration(id: String, newDurationMillis: Long) {
        when (id) {
            "timer0" -> timer0.value = timer0.value.copy(durationMillis = newDurationMillis)
            "timer1" -> timer1.value = timer1.value.copy(durationMillis = newDurationMillis)
            "timer2" -> timer2.value = timer2.value.copy(durationMillis = newDurationMillis)
            else -> throw IllegalArgumentException("Invalid timerId")
        }
    }
}