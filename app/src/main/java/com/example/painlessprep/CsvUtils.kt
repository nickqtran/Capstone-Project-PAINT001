package com.example.painlessprep

import java.io.File
import android.content.Context

/**
 * An object class meant for helping with csv formatting and whatnot.
 */
object CsvUtils {

    /**
     * Cleanly creates a csv string using a WindowData object
     *
     *@param[measurement] a WindowData object that holds the measurement name, width, and height
     */
    fun formatCsvString(measurement: WindowData) : String{
        val formattedMeasurement : String = "${measurement.name},${measurement.width},${measurement.height},${measurement.amount}\n"
        return formattedMeasurement
    }

    /**
     * Checks the existence of a CSV file, and creates one if one is not found.
     *
     * @param[name] The name of the csv file to create WITHOUT the .csv on the end (EX. 'measurements.csv' would pass 'measurements')
     * @return The CSV file we have created/checked for the existence of.
     */
    fun checkCsv(name: String, context: Context) : File {
        val csvFile = File(context.filesDir, "${name}.csv")
        if(!csvFile.exists()) {
            csvFile.writeText("name,width,height,amount\n")
        }

        return csvFile
    }
}