package com.cgm.timetwist

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log

object VibrationManager {
    private lateinit var vibrator: Vibrator
    private var isInitialized: Boolean = false

    fun initialize(context: Context) {
        val vibratorManager =
            context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager
        vibrator = vibratorManager.defaultVibrator

        if (vibrator.hasVibrator()) {
            isInitialized = true
            Log.d("VibrationManager", "Vibrator initialized successfully.")
        } else {
            Log.e("VibrationManager", "Device does not support vibration.")
        }
    }

    fun vibrateClick() {
        if (isInitialized) {
            val offOn: Long = 50
            val pattern = longArrayOf(0, offOn, offOn, offOn)
            val effect = VibrationEffect.createWaveform(pattern, -1)
            vibrator.vibrate(effect)
            Log.d("VibrationManager", "Click vibration triggered.")
        } else {
            Log.w("VibrationManager", "Vibrator not initialized or unsupported.")
        }
    }

    fun vibrateHeavyClick() {
        if (isInitialized) {
            val pattern = longArrayOf(0, 700)
            val effect = VibrationEffect.createWaveform(pattern, -1)
            vibrator.vibrate(effect)
            Log.d("VibrationManager", "Heavy click vibration triggered.")
        } else {
            Log.w("VibrationManager", "Vibrator not initialized or unsupported.")
        }
    }

    fun vibrateTick() {
        if (isInitialized) {
            val tickEffect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
            vibrator.vibrate(tickEffect)
            Log.d("VibrationManager", "Tick vibration triggered.")
        } else {
            Log.w("VibrationManager", "Vibrator not initialized or unsupported.")
        }
    }

    fun vibrateCustom(pattern: LongArray, repeat: Int = -1) {
        if (isInitialized) {
            val customEffect = VibrationEffect.createWaveform(pattern, repeat)
            vibrator.vibrate(customEffect)
            Log.d("VibrationManager", "Custom vibration pattern triggered.")
        } else {
            Log.w("VibrationManager", "Vibrator not initialized or unsupported.")
        }
    }

    fun cancel() {
        if (isInitialized) {
            vibrator.cancel()
            Log.d("VibrationManager", "Vibration canceled.")
        } else {
            Log.w("VibrationManager", "Vibrator not initialized or unsupported.")
        }
    }
}
