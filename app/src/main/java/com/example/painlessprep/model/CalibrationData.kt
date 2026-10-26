package com.example.painlessprep.model

import org.opencv.core.Mat

/**
 * Acts as the storage device for our calibration data
 *
 * @param[cameraMatrix] The camera matrix data obtained through calibration
 * @param[distortionCoeffs] The camera distortion coefficients obtained through calibration
 * @param[rms] The calibration error number to show how well calibration worked
 */
data class CalibrationData(
    val cameraMatrix: Mat,
    val distortionCoeffs: Mat,
    val rms: Double
)
