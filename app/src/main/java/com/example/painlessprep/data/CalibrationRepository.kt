package com.example.painlessprep.data

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.example.painlessprep.model.CalibrationData
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.json.JSONArray
import org.json.JSONObject

class CalibrationRepository(private val context: Context) {

    fun saveCalibration(cameraMatrix: Mat, distortionCoeffs: Mat, rms: Double) {
        val prefs = context.getSharedPreferences("CameraPrefs", MODE_PRIVATE)

        val cameraArray = DoubleArray(9)
        cameraMatrix.get(0, 0, cameraArray)

        val distArray = DoubleArray(distortionCoeffs.total().toInt())
        distortionCoeffs.get(0, 0, distArray)

        val json = JSONObject().apply {
            put("cameraMatrix", JSONArray(cameraArray.toList()))
            put("distanceCoeffs", JSONArray(distArray.toList()))
            put("rmsValue", rms)
        }

        prefs.edit().putString("calibration", json.toString()).apply()
    }

    fun loadCalibration(): CalibrationData? {
        val prefs = context.getSharedPreferences("CameraPrefs", MODE_PRIVATE)
        val jsonString = prefs.getString("calibration", null) ?: return null
        val json = JSONObject(jsonString)

        val cameraArray = json.getJSONArray("cameraMatrix")
        val cameraMatrix = Mat(3, 3, CvType.CV_64F)
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                cameraMatrix.put(i, j, cameraArray.getDouble(i * 3 + j))
            }
        }

        val distArray = json.getJSONArray("distanceCoeffs")
        val distortionCoeffs = Mat(distArray.length(), 1, CvType.CV_64F)
        for (i in 0 until distArray.length()) {
            distortionCoeffs.put(i, 0, distArray.getDouble(i))
        }

        val rms = json.getDouble("rmsValue")
        return CalibrationData(cameraMatrix, distortionCoeffs, rms)
    }
}
