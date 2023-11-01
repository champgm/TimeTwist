package com.example.timetwist.presentation

data class DraggerState(
    val offset: DoubleOffset = DoubleOffset(0.0, 0.0),
    val isInitialized: Boolean
)