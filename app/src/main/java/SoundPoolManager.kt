package com.cgm.timetwist

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log

object SoundPoolManager {
    private lateinit var soundPool: SoundPool
    private var soundIdClick: Int = 0
    private var soundIdCrunch: Int = 0
    private var soundLoaded: Boolean = false

    fun initialize(context: Context) {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()

        soundIdClick = soundPool.load(context, R.raw.click_sound, 1)
        soundIdCrunch = soundPool.load(context, R.raw.potato_chip, 1)

        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                if (sampleId == soundIdClick || sampleId == soundIdCrunch) {
                    soundLoaded = true
                    Log.d("SoundPool", "Sound loaded successfully.")
                }
            } else {
                Log.e("SoundPool", "Failed to load sound.")
            }
        }
    }

    fun playClickSound() {
        if (soundLoaded) {
            soundPool.play(soundIdClick, 1f, 1f, 0, 0, 1f)
        } else {
            Log.w("SoundPool", "Click sound not loaded yet.")
        }
    }

    fun playCrunchSound() {
        if (soundLoaded) {
            soundPool.play(soundIdCrunch, 1f, 1f, 0, 0, 1f)
        } else {
            Log.w("SoundPool", "Crunch sound not loaded yet.")
        }
    }

    fun release() {
        soundPool.release()
    }
}