package com.cgm.timetwist.presentation

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.cgm.timetwist.service.TimeDetails
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TimerPreferencesTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        context.getSharedPreferences("time_twist_preferences", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @After
    fun tearDown() {
        context.getSharedPreferences("time_twist_preferences", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @Test
    fun `saveTimerDetails should round trip persisted fields`() {
        val details = TimeDetails(
            timerId = "timer1",
            durationMillis = 123_000L,
            repeating = true,
            vibration = true,
            sound = false,
            intervalStuff = false,
        )

        saveTimerDetails(context, "timer1", details)

        assertThat(getTimerDetails(context, "timer1")).isEqualTo(
            TimeDetails(
                timerId = "timer1",
                durationMillis = 123_000L,
                repeating = true,
                vibration = true,
                sound = false,
                intervalStuff = false,
            )
        )
    }

    @Test
    fun `getTimerDetails should return null when duration is missing`() {
        assertThat(getTimerDetails(context, "timer2")).isNull()
    }

    @Test
    fun `getTimerDetails should preserve persisted booleans`() {
        saveTimerDetails(
            context = context,
            timerId = "timer0",
            details = TimeDetails(
                timerId = "timer0",
                durationMillis = 30_000L,
                repeating = false,
                vibration = false,
                sound = true,
                intervalStuff = true,
            )
        )

        val restored = getTimerDetails(context, "timer0")

        assertThat(restored?.repeating).isFalse()
        assertThat(restored?.vibration).isFalse()
        assertThat(restored?.sound).isTrue()
        assertThat(restored?.intervalStuff).isTrue()
    }
}
