package com.cgm.timetwist.presentation

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cgm.timetwist.ui.EditScreen
import com.cgm.timetwist.ui.WearApp

val buttonPadding = 4.dp
val googleYellow = Color(0xFFFFEB3B)
val googleRed = Color(0xFFF44336)
val googleGreen = Color(0xFF4CAF50)
val googleBlue = Color(0xFF2196F3)
fun muteColor(originalColor: Color, factor: Float): Color {
    return lerp(originalColor, Color.Gray, factor)
}

const val muteFactor = 0.4f
val mutedGoogleYellow = muteColor(Color(0xFFFFEB3B), muteFactor)
val mutedGoogleRed = muteColor(Color(0xFFF44336), muteFactor)
val mutedGoogleGreen = muteColor(Color(0xFF4CAF50), muteFactor)
val mutedGoogleBlue = muteColor(Color(0xFF2196F3), muteFactor)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            val timerViewModel: TimerViewModel = viewModel()
            val navController = rememberNavController()
            NavHost(navController, startDestination = "main") {
                composable("main") {
                    WearApp(this@MainActivity, navController, timerViewModel)
                }
                composable("edit/{timerId}") { backStackEntry ->
                    val timerId = backStackEntry.arguments?.getString("timerId") ?: "timer0"
                    EditScreen(timerId, navController, timerViewModel)
                }
            }
        }
    }
}

