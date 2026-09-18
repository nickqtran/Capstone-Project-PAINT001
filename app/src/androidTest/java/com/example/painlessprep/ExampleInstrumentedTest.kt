package com.example.painlessprep

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

import java.io.File

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class csvUtilsInstrumentedTests {
    @Test
    fun testCsvCheck() {
        //Create our context and filename
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val fileName = "TestFileName"

        //Make sure any previous test files are destroyed
        val file = File(context.filesDir, fileName)
        if(file.exists()) {
            file.delete()
        }

        //Run the function
        val testFile = CsvUtils.checkCsv(fileName, context)

        //Verify it worked
        assertTrue(testFile.exists())
    }
}