package com.cgm.timetwist.service

fun Long.getTime(): String {
    val minutes = this / 60000
    val minutesString = if (minutes < 10) "0$minutes" else "$minutes"
    val leftoverSeconds = this % 60000
    val seconds = leftoverSeconds / 1000
    val secondsString = if (seconds < 10) "0$seconds" else "$seconds"
    return "$minutesString:$secondsString"
}

fun Long.getMinutes(): String {
    val minutes = this / 60000
    val minutesString = if (minutes < 10) "0$minutes" else "$minutes"
    return minutesString
}

fun Long.getSeconds(): String {
    val leftoverSeconds = this % 60000
    val seconds = leftoverSeconds / 1000
    val secondsString = if (seconds < 10) "0$seconds" else "$seconds"
    return secondsString
}