package com.example.timetwist.presentation

data class CircularSliderState(
    val center: DoubleOffset,
    val radius: Double,
    val isInitialized: Boolean = false,
)