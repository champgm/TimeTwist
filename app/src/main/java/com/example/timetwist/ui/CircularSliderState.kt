package com.example.timetwist.ui

data class CircularSliderState(
    val center: DoubleOffset,
    val radius: Double,
    val isInitialized: Boolean = false,
)