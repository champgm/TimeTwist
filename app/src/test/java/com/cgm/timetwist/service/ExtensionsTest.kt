package com.cgm.timetwist.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ExtensionsTest {

    @Test
    fun `getTime should format representative durations`() {
        assertThat(0L.getTime()).isEqualTo("00:00")
        assertThat(5_000L.getTime()).isEqualTo("00:05")
        assertThat(65_000L.getTime()).isEqualTo("01:05")
        assertThat(3_726_000L.getTime()).isEqualTo("62:06")
    }

    @Test
    fun `getMinutes should format representative durations`() {
        assertThat(0L.getMinutes()).isEqualTo("00")
        assertThat(59_000L.getMinutes()).isEqualTo("00")
        assertThat(60_000L.getMinutes()).isEqualTo("01")
        assertThat(3_726_000L.getMinutes()).isEqualTo("62")
    }

    @Test
    fun `getSeconds should format representative durations`() {
        assertThat(0L.getSeconds()).isEqualTo("00")
        assertThat(5_000L.getSeconds()).isEqualTo("05")
        assertThat(65_000L.getSeconds()).isEqualTo("05")
        assertThat(3_726_000L.getSeconds()).isEqualTo("06")
    }
}
