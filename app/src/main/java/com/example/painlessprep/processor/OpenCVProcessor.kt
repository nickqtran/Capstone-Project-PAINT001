package com.example.painlessprep.processor

import org.opencv.calib3d.Calib3d
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Size
import org.opencv.core.TermCriteria
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.ArucoDetector
import org.opencv.objdetect.DetectorParameters
import org.opencv.objdetect.Objdetect

class OpenCVProcessor {

    private val dictionary = Objdetect.getPredefinedDictionary(Objdetect.DICT_4X4_50);
    private val parameters = DetectorParameters();
    private val arucoDetector = ArucoDetector(dictionary, parameters);
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

    fun processCalibrationFrame(
        gray: Mat,
        rgba: Mat,
        boardSize: Size,
        calibSquareSize: Double
    ): MatOfPoint2f? {
        val corners = MatOfPoint2f()
        val foundBoard = Calib3d.findChessboardCorners(
            gray, boardSize, corners,
            Calib3d.CALIB_CB_ADAPTIVE_THRESH or Calib3d.CALIB_CB_NORMALIZE_IMAGE
        )

        if (foundBoard) {
            Imgproc.cornerSubPix(
                gray, corners,
                Size(11.0, 11.0),
                Size(-1.0, -1.0),
                TermCriteria(TermCriteria.EPS + TermCriteria.MAX_ITER, 50, 0.001)
            )
            Calib3d.drawChessboardCorners(rgba, boardSize, corners, foundBoard)
            return corners
        }
        return null
    }

    fun processArucoFrame(
        gray: Mat,
        rgba: Mat,
        cameraMatrix: Mat?,
        distortionCoeffs: Mat?
    ): Pair<Double, Double> {
        val corners = ArrayList<Mat>()
        val ids = Mat()
        arucoDetector.detectMarkers(gray, corners, ids)

        var width = 0.0
        var height = 0.0

        if (!ids.empty() && cameraMatrix != null && distortionCoeffs != null) {
            // ArUco detection, solvePnP, and distance calculations...
        }
        return Pair(width, height)
    }
}
