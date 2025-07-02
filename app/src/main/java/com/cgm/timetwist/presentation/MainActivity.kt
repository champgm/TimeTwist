package com.cgm.timetwist.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cgm.timetwist.SoundPoolManager
import com.cgm.timetwist.VibrationManager
import com.cgm.timetwist.ui.EditScreen
import com.cgm.timetwist.ui.WearApp

val buttonPadding = 1.dp
val googleYellow = Color(0xFFFBBC05)
val googleRed = Color(0xFFEA4335)
val googleGreen = Color(0xFF34A853)
val googleBlue = Color(0xFF4285F4)
val black = Color(0xFF000000)
val white = Color(0xFFFFFFFF)
fun muteColor(originalColor: Color, factor: Float): Color {
    return lerp(originalColor, Color.Gray, factor)
}

const val muteFactor = 0.4f
val mutedGoogleYellow = muteColor(googleYellow, muteFactor)
val mutedGoogleRed = muteColor(googleRed, muteFactor)
val mutedGoogleGreen = muteColor(googleGreen, muteFactor)
val mutedGoogleBlue = muteColor(googleBlue, muteFactor)
val mutedBlack = muteColor(black, muteFactor)
val mutedWhite = muteColor(white, muteFactor)


class MainActivity : ComponentActivity() {

    // Launcher for requesting the notification permission.
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.i("Permission", "POST_NOTIFICATIONS permission granted.")
                // Permission is granted. You can now safely post notifications.
            } else {
                Log.w("Permission", "POST_NOTIFICATIONS permission denied.")
                // Explain to the user that the feature is unavailable because the
                // feature requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their decision.
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        SoundPoolManager.initialize(this)
        VibrationManager.initialize(this)

        // Check and request notification permission on Android 13+
        checkAndRequestNotificationPermission()

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

    private fun checkAndRequestNotificationPermission() {
        // Check if the Android version is 13 (TIRAMISU) or higher
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is already granted
                Log.i("Permission", "POST_NOTIFICATIONS permission already granted.")
            }

            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                // Explain to the user why you need the permission.
                // In an actual app, you might show a dialog here.
                Log.w("Permission", "Showing rationale for POST_NOTIFICATIONS permission.")
                // Then, request the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }

            else -> {
                // Directly ask for the permission
                Log.i("Permission", "Requesting POST_NOTIFICATIONS permission.")
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

