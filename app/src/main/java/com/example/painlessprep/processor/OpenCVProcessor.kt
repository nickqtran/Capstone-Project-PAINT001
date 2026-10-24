package com.example.painlessprep.processor

import org.opencv.calib3d.Calib3d
import org.opencv.core.Mat
import org.opencv.core.Size

class OpenCVProcessor {
    fun runCalibration(
        objectPointCast: List<Mat>,
        imagePointCast: List<Mat>,
        imageSize: Size,
        cameraMatrix: Mat,
        distortionCoeffs: Mat,
        rvecs: MutableList<Mat>,
        tvecs: MutableList<Mat>
    ): Double {
        val flags = Calib3d.CALIB_RATIONAL_MODEL
        return Calib3d.calibrateCamera(
            objectPointCast,
            imagePointCast,
            imageSize,
            cameraMatrix,
            distortionCoeffs,
            rvecs,
            tvecs,
            flags
        )
    }
}
