package com.example.timetwist.ui

data class DraggerState(
    val offset: DoubleOffset = DoubleOffset(0.0, 0.0),
    val isInitialized: Boolean
)