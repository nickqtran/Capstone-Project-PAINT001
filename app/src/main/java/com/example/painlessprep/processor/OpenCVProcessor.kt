package com.example.painlessprep.processor

import org.opencv.calib3d.Calib3d
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.ArucoDetector
import org.opencv.objdetect.DetectorParameters
import org.opencv.objdetect.Objdetect
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.math.sqrt

class OpenCVProcessor {

    private val dictionary by lazy { Objdetect.getPredefinedDictionary(Objdetect.DICT_4X4_50) }
    private val parameters by lazy { DetectorParameters() }
    private val arucoDetector by lazy { ArucoDetector(dictionary, parameters) }

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
                gray,
                corners,
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
        val markerTvecs = mutableListOf<Pair<Int, Mat>>()

        if (!ids.empty() && cameraMatrix != null && distortionCoeffs != null) {
            for (i in corners.indices) {
                val corner = corners[i]
                val markerId = ids[i, 0][0].toInt()

                val pts = corner.reshape(2, 4)
                val imagePoints = MatOfPoint2f(pts)
                val markerSize = 0.05
                val halfSize = markerSize / 2.0

                val objectPoints = MatOfPoint3f(
                    Point3(-halfSize, halfSize, 0.0),
                    Point3(halfSize, halfSize, 0.0),
                    Point3(halfSize, -halfSize, 0.0),
                    Point3(-halfSize, -halfSize, 0.0)
                )

                val rvec = Mat()
                val tvec = Mat()
                val dCoeffMatOfDouble = MatOfDouble()
                val distArray = DoubleArray(distortionCoeffs.rows()) { idx ->
                    distortionCoeffs.get(idx, 0)[0]
                }
                dCoeffMatOfDouble.fromArray(*distArray)

                Calib3d.solvePnP(objectPoints, imagePoints, cameraMatrix, dCoeffMatOfDouble, rvec, tvec)
                markerTvecs.add(Pair(markerId, tvec))

                val distanceFromCamera = sqrt(
                    tvec.get(0, 0)[0].pow(2) +
                            tvec.get(1, 0)[0].pow(2) +
                            tvec.get(2, 0)[0].pow(2)
                )

                val points = MatOfPoint(
                    Point(corner.get(0, 0)[0], corner.get(0, 0)[1]),
                    Point(corner.get(0, 1)[0], corner.get(0, 1)[1]),
                    Point(corner.get(0, 2)[0], corner.get(0, 2)[1]),
                    Point(corner.get(0, 3)[0], corner.get(0, 3)[1])
                )
                Imgproc.polylines(rgba, listOf(points), true, Scalar(0.0, 255.0, 0.0, 3.0))

                val centerX = (corner.get(0, 0)[0] + corner.get(0, 2)[0]) / 2
                val centerY = (corner.get(0, 0)[1] + corner.get(0, 2)[1]) / 2

                Imgproc.putText(
                    rgba,
                    "ID: $markerId : Dist: %.2f m".format(distanceFromCamera),
                    Point(centerX, centerY),
                    Imgproc.FONT_HERSHEY_SIMPLEX,
                    1.0,
                    Scalar(255.0, 0.0, 0.0),
                    2
                )
            }

            var lineIndex = 0
            for (i in 0 until markerTvecs.size) {
                for (j in i + 1 until markerTvecs.size) {
                    val (id1, tvec1) = markerTvecs[i]
                    val (id2, tvec2) = markerTvecs[j]

                    if ((id1 == 0 && id2 == 2) || (id1 == 2 && id2 == 0)) continue

                    val dx = tvec2.get(0, 0)[0] - tvec1.get(0, 0)[0]
                    val dy = tvec2.get(1, 0)[0] - tvec1.get(1, 0)[0]
                    val dz = tvec2.get(2, 0)[0] - tvec1.get(2, 0)[0]

                    val markerDistance = (sqrt(dx * dx + dy * dy + dz * dz) * 39.37) + (2 + (0.25 * 2))
                    val rounded = round(markerDistance * 32) / 32

                    if ((id1 == 0 && id2 == 1) || (id1 == 1 && id2 == 0)) {
                        Imgproc.putText(
                            rgba,
                            "Height: ${decimalTo32nds(rounded)}",
                            Point(50.0, 50.0 + (30.0 * lineIndex)),
                            Imgproc.FONT_HERSHEY_SIMPLEX,
                            0.8,
                            Scalar(5.0, 252.0, 244.0),
                            2
                        )
                        height = "%.4f".format(rounded).toDouble()
                    } else if ((id1 == 1 && id2 == 2) || (id1 == 2 && id2 == 1)) {
                        Imgproc.putText(
                            rgba,
                            "Width: ${decimalTo32nds(rounded)}",
                            Point(50.0, 50.0 + (30.0 * lineIndex)),
                            Imgproc.FONT_HERSHEY_SIMPLEX,
                            0.8,
                            Scalar(5.0, 252.0, 244.0),
                            2
                        )
                        width = "%.4f".format(rounded).toDouble()
                    }
                    lineIndex++
                }
            }
        }
        return Pair(width, height)
    }

    private fun decimalTo32nds(inches: Double): String {
        val wholeInches = inches.toInt()
        val remainder = inches - wholeInches
        val thirtySeconds = (remainder * 32).roundToInt()

        return when (thirtySeconds) {
            0 -> "$wholeInches\""
            32 -> "${wholeInches + 1}\""
            else -> "$wholeInches $thirtySeconds/32\""
        }
    }
}
