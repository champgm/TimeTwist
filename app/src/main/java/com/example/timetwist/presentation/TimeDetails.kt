package com.example.timetwist.presentation

data class TimeDetails(
    var durationMillis: Long = 1,
    var startTime: Long = 1,
    var elapsedTime: Long = 1,
    var timeRemaining: Long = 1,
//    var secondsRemaining: Int = 1,
    var started: Boolean = false,
) {

}