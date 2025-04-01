package com.cgm.timetwist.ui

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.cgm.timetwist.presentation.TimerViewModel
import com.cgm.timetwist.presentation.buttonPadding
import com.cgm.timetwist.presentation.googleBlue
import com.cgm.timetwist.presentation.googleGreen
import com.cgm.timetwist.presentation.googleRed
import com.cgm.timetwist.presentation.googleYellow
import com.cgm.timetwist.presentation.mutedGoogleBlue
import com.cgm.timetwist.presentation.mutedGoogleGreen
import com.cgm.timetwist.presentation.mutedGoogleRed
import com.cgm.timetwist.presentation.mutedGoogleYellow
import com.cgm.timetwist.presentation.theme.TimeTwistTheme
import androidx.compose.runtime.*
import com.cgm.timetwist.presentation.black
import com.cgm.timetwist.presentation.white
import com.cgm.timetwist.presentation.mutedBlack
import com.cgm.timetwist.presentation.mutedWhite
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size

private class TriangleShape(private val isTopRight: Boolean) : Shape {
    val gap =  5f
    override fun createOutline(
        size: Size,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
        density: androidx.compose.ui.unit.Density
    ): Outline {
        val path = Path().apply {
            if (isTopRight) {
                moveTo(gap, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width, size.height-gap)
            } else {
                moveTo(0f, gap)
                lineTo(0f, size.height)
                lineTo(size.width-gap, size.height)
            }
            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
fun WearApp(context: Context, navController: NavController, timerViewModel: TimerViewModel) {
    Log.d("WearApp", "TimerViewModel HashCode: ${timerViewModel.hashCode()}")

    TimeTwistTheme {
        var inEditMode by remember { mutableStateOf(false) }
        var darkMode by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (darkMode) mutedWhite else black),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val coroutineScope = rememberCoroutineScope()
            Row(
                // 1
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(end = buttonPadding, bottom = buttonPadding)
                        .weight(1f)
                ) {
                    // Top-right triangle (Settings button)
                    Button(
                        onClick = {
                            timerViewModel.stopTimers(context)
                            inEditMode = !inEditMode
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = when {
                                inEditMode && darkMode -> mutedBlack
                                inEditMode -> mutedGoogleRed
                                darkMode -> black
                                else -> googleRed
                            }
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(TriangleShape(true)),
                        shape = MaterialTheme.shapes.large.copy(CornerSize(0.dp))
                    ) {
                        Text(
                            text = if (inEditMode) "✅" else "⚙️",
                            modifier = Modifier.offset(x = 24.dp, y = (-18).dp),
                            color = if (darkMode) white else black
                        )
                    }

                    // Bottom-left triangle (Theme toggle button)
                    Button(
                        onClick = {
                            darkMode = !darkMode
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = when {
                                inEditMode && darkMode -> mutedBlack
                                inEditMode -> mutedGoogleRed
                                darkMode -> black
                                else -> googleRed
                            }
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(TriangleShape(false)),
                        shape = MaterialTheme.shapes.large.copy(CornerSize(0.dp))
                    ) {
                        Text(
                            text = if (darkMode) "🌑" else "☀️",
                            modifier = Modifier.offset(x = (-18).dp, y = 24.dp),
                            color = if (darkMode) white else black
                        )
                    }
                }

                TimerButton(
                    editModeColor = if (darkMode) mutedBlack else mutedGoogleBlue,
                    color = if (darkMode) black else googleBlue,
                    inEditMode = inEditMode,
                    navController = navController,
                    timerViewModel = timerViewModel,
                    timerState = timerViewModel.timer0,
                    timerId = "timer0",
                    context = context,
                    coroutineScope = coroutineScope,
                    buttonModifier = Modifier
                        .fillMaxSize()
                        .padding(start = buttonPadding, bottom = buttonPadding)
                        .weight(1f),
                    textModifier = Modifier.offset(x = -buttonPadding, y = buttonPadding * 3),
                    textColor = if (darkMode) white else black
                )
            }
            Row(
                // 2
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                TimerButton(
                    editModeColor = if (darkMode) mutedBlack else mutedGoogleYellow,
                    color = if (darkMode) black else googleYellow,
                    inEditMode = inEditMode,
                    navController = navController,
                    timerViewModel = timerViewModel,
                    timerState = timerViewModel.timer1,
                    timerId = "timer1",
                    context = context,
                    coroutineScope = coroutineScope,
                    buttonModifier = Modifier
                        .fillMaxSize()
                        .padding(end = buttonPadding, top = buttonPadding)
                        .weight(1f),
                    textModifier = Modifier.offset(x = buttonPadding, y = -buttonPadding * 3),
                    textColor = if (darkMode) white else black,
                )
                TimerButton(
                    editModeColor = if (darkMode) mutedBlack else mutedGoogleGreen,
                    color = if (darkMode) black else googleGreen,
                    inEditMode = inEditMode,
                    navController = navController,
                    timerViewModel = timerViewModel,
                    timerState = timerViewModel.timer2,
                    timerId = "timer2",
                    context = context,
                    coroutineScope = coroutineScope,
                    buttonModifier = Modifier
                        .fillMaxSize()
                        .padding(start = buttonPadding, top = buttonPadding)
                        .weight(1f),
                    textModifier = Modifier.offset(x = -buttonPadding, y = -buttonPadding * 3),
                    textColor = if (darkMode) white else black,
                )
            }
        }
    }
}
