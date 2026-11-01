package com.example.painlessprep.data

import android.content.Context
import com.example.painlessprep.processor.CsvUtils
import com.example.painlessprep.model.WindowData

class MeasurementRepository(private val context: Context) {

    fun saveMeasurement(windowData: WindowData) {
        val measurementCsv = CsvUtils.formatCsvString(windowData)
        val csvFile = CsvUtils.checkCsv("measurements", context)
        csvFile.appendText(measurementCsv)
    }
}
