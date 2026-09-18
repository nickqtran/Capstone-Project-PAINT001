package com.example.painlessprep

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}


/**
 * Pane Perfect (Painless Prep) Tests
 */

class PanePerfectTest {
    @Test
    fun testCsvFormatting() {
        val data : WindowData = WindowData("Kitchen Wall",12.5,22.6,15)

        val formattedString = CsvUtils.formatCsvString(data)
        assertEquals("Kitchen Wall,12.5,22.6,15\n", formattedString)
    }
}