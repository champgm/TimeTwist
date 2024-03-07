package com.cgm.timetwist.service

data class TimeDetails(
    val timerId: String,
    var durationMillis: Long = 1,
    var startTime: Long = 1,
    var elapsedTime: Long = 1,
    var timeRemaining: Long = 1,
    var repeating: Boolean = false,
    var started: Boolean = false,
)