package com.example.painlessprep.processor

import org.opencv.calib3d.Calib3d
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.ArucoDetector
import org.opencv.objdetect.DetectorParameters
import org.opencv.objdetect.Objdetect
import java.lang.Math.toDegrees
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.math.sqrt

interface ProcessorInfo {
    var currentWidth: String
    var currentHeight: String
    var squareConfidence: Double
    var markerConfidence: Map<Int, Double>
}


class OpenCVProcessor {

    private val dictionary by lazy { Objdetect.getPredefinedDictionary(Objdetect.DICT_4X4_50) }
    private val parameters by lazy { DetectorParameters() }
    private val arucoDetector by lazy { ArucoDetector(dictionary, parameters) }

    private val WARN_SCALAR = Scalar(255.0, 255.0, 0.0)

    private val GOOD_SCALAR = Scalar(0.0, 255.0, 0.0)

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


    /**
     * Calculates a tracking confidence rating for a rectangular framework formed by 3 ArUco markers.
     * Automatically identifies the layout orientation and handles uneven width/height variations.
     */
    fun evaluateDynamicRectangleConfidence(
        corners: List<Mat>,
        ids: Mat
    ): Double {
        // Basic verification: We need exactly 3 markers to perform the verification
        if (ids.empty() || corners.isEmpty() || ids.rows() < 3) return 0.0

        // 1. Extract 2D pixel coordinates using primitives to avoid any matrix pointer traps
        val centers = ArrayList<Point>()
        val totalDetected = min(ids.rows(), corners.size)

        for (i in 0 until totalDetected) {
            val cornerMat = corners[i]

            val d0 = cornerMat.get(0, 0) ?: continue
            val d1 = cornerMat.get(0, 1) ?: continue
            val d2 = cornerMat.get(0, 2) ?: continue
            val d3 = cornerMat.get(0, 3) ?: continue

            val cx = (d0[0] + d1[0] + d2[0] + d3[0]) / 4.0
            val cy = (d0[1] + d1[1] + d2[1] + d3[1]) / 4.0
            centers.add(Point(cx, cy))
        }

        if (centers.size < 3) return 0.0

        val pA = centers[0]
        val pB = centers[1]
        val pC = centers[2]

        // 2. Identify the corner vertex using the longest side (the diagonal hypotenuse)
        val distAB = sqrt((pB.x - pA.x).pow(2) + (pB.y - pA.y).pow(2))
        val distBC = sqrt((pC.x - pB.x).pow(2) + (pC.y - pB.y).pow(2))
        val distCA = sqrt((pA.x - pC.x).pow(2) + (pA.y - pC.y).pow(2))
        val maxDist = max(distAB, max(distBC, distCA))

        val cornerPoint: Point
        val endPoint1: Point
        val endPoint2: Point

        when (maxDist) {
            distBC -> { cornerPoint = pA; endPoint1 = pB; endPoint2 = pC }
            distCA -> { cornerPoint = pB; endPoint1 = pA; endPoint2 = pC }
            else   -> { cornerPoint = pC; endPoint1 = pA; endPoint2 = pB }
        }

        // In any perspective projection of a rectangle, the interal angle
        // formed at the corner vertex must satisfy the projection
        // constraint with the remaining layout vertices.
        val side1Sq = (endPoint1.x - cornerPoint.x).pow(2) + (endPoint1.y - cornerPoint.y).pow(2)
        val side2Sq = (endPoint2.x - cornerPoint.x).pow(2) + (endPoint2.y - cornerPoint.y).pow(2)
        val diagSq = (endPoint1.x - endPoint2.x).pow(2) + (endPoint1.y - endPoint2.y).pow(2)

        // Calculate how close the triangle is to a right-angled intersection (a² + b² = c²)
        val expectedDiagSq = side1Sq + side2Sq

        // If the points are placed in a random triangle layout, the variance between
        // expected and actual diagonal lengths blows up instantly.
        val varianceRatio = min(expectedDiagSq, diagSq) / max(expectedDiagSq, diagSq)

        // Ensure markers aren't all on single line
        val area = abs(cornerPoint.x * (endPoint1.y - endPoint2.y) + endPoint1.x * (endPoint2.y - cornerPoint.y) + endPoint2.x * (cornerPoint.y - endPoint1.y)) / 2.0
        val minRequiredArea = maxDist.pow(2) * 0.05 // Triangle must have a meaningful interior volume

        if (area < minRequiredArea) return 0.0

        // smooth out confidence
        val confidence = max(0.0, (varianceRatio - 0.5) * 2.0)

        return Math.round(confidence * 100.0) / 100.0
    }


    /**
     * Calculates a custom confidence score for detected ArUco markers.
     *
     * @param corners List of detected marker corners from ArUco detection.
     * @param ids Mat containing the IDs of the detected markers.
     * @param rvecs Mat containing rotation vectors (from estimatePoseSingleMarkers).
     * @param tvecs Mat containing translation vectors (from estimatePoseSingleMarkers).
     * @param cameraMatrix Camera intrinsic matrix.
     * @param distCoeffs Camera distortion coefficients.
     * @param markerLength The physical length of the marker side (e.g., 0.05 meters).
     * @return A map linking Marker ID to a confidence rating between 0.0 (poor) and 1.0 (perfect).
     */
    fun calculateConfidence(
        corners: List<Mat>,
        ids: Mat,
        rvecs: List<Pair<Int,Mat>>,
        tvecs: List<Pair<Int,Mat>>,
        cameraMatrix: Mat,
        distCoeffs: MatOfDouble,
        markerLength: Double = 0.05
    ): Map<Int, Double> {

        val confidenceScores = HashMap<Int, Double>()
        if (ids.empty() || corners.isEmpty()) return confidenceScores

        // Define 3D coordinates of a standard square marker centered at its origin
        val halfLength = markerLength / 2.0
        val objPointsMat = MatOfPoint3f(
            Point3(-halfLength, halfLength, 0.0),
            Point3(halfLength, halfLength, 0.0),
            Point3(halfLength, -halfLength, 0.0),
            Point3(-halfLength, -halfLength, 0.0)
        )

        val rvecMap = rvecs.toMap()
        val tvecMap = tvecs.toMap()

        val totalMarkers = ids.rows()
        for (i in 0 until totalMarkers) {
            // Extract Marker ID
            val markerId = ids.get(i, 0)[0].toInt()

            // Extract 2D detected corners for the current marker
            val markerCornersMat = MatOfPoint2f()
            corners[i].convertTo(markerCornersMat, CvType.CV_32F)
            val cornerPoints = markerCornersMat.toArray()

            // Extract individual rvec and tvec rows for this marker
            val rvec = rvecMap[markerId]
            val tvec = tvecMap[markerId]

            // 1. Calculate Reprojection Error
            val projectedPointsMat = MatOfPoint2f()
            Calib3d.projectPoints(objPointsMat, rvec, tvec, cameraMatrix, distCoeffs, projectedPointsMat)
            val projectedPoints = projectedPointsMat.toArray()

            var totalReprojError = 0.0
            for (j in 0..3) {
                val dist = Core.norm(MatOfPoint2f(cornerPoints[j]), MatOfPoint2f(projectedPoints[j]))
                totalReprojError += dist
            }
            val meanReprojError = totalReprojError / 4.0

            // 2. Geometric Squareness Check (Side Length Consistency)
            val sides = DoubleArray(4)
            sides[0] = Core.norm(MatOfPoint2f(cornerPoints[0]), MatOfPoint2f(cornerPoints[1]))
            sides[1] = Core.norm(MatOfPoint2f(cornerPoints[1]), MatOfPoint2f(cornerPoints[2]))
            sides[2] = Core.norm(MatOfPoint2f(cornerPoints[2]), MatOfPoint2f(cornerPoints[3]))
            sides[3] = Core.norm(MatOfPoint2f(cornerPoints[3]), MatOfPoint2f(cornerPoints[0]))

            // Calculate variance of side lengths
            val avgSideLength = sides.average()
            val sideVariance = sides.map { (it - avgSideLength).pow(2) }.average()

            // 3. Compile Rating (Scale scores cleanly between 0.0 and 1.0)
            // Penalize geometry if sides vary significantly relative to overall marker size
            val geomScore = max(0.0, 1.0 - (sideVariance / avgSideLength.pow(2)))
            // Penalize reprojection heavily if average error exceeds 5.0 pixels
            val reprojScore = max(0.0, 1.0 - (meanReprojError / 5.0))

            // Weighted final confidence (60% alignment, 40% geometry)
            val finalConfidence = (geomScore * 0.4) + (reprojScore * 0.6)

            // Round to 2 decimal places
            confidenceScores[markerId] = Math.round(finalConfidence * 100.0) / 100.0

            // Clean up native row slices

            markerCornersMat.release()
            projectedPointsMat.release()
        }

        objPointsMat.release()
        return confidenceScores
    }

    fun processArucoFrame(
        gray: Mat,
        rgba: Mat,
        cameraMatrix: Mat?,
        distortionCoeffs: Mat?
    ): ProcessorInfo {
        val corners = ArrayList<Mat>()
        val ids = Mat()
        arucoDetector.detectMarkers(gray, corners, ids)

        val squareConfidences = evaluateDynamicRectangleConfidence(corners, ids)
        val rectangeConfidence = GeometryUtils.diagnoseRectangleLayout(corners, ids)
        var markerConfidences = emptyMap<Int, Double>()
        var formatWidth = ""
        var formatHeight = ""

        var width = 0.0
        var height = 0.0
        val markerTvecs = mutableListOf<Pair<Int, Mat>>()
        val markerRvecs = mutableListOf<Pair<Int,Mat>>()

        if (!ids.empty() && cameraMatrix != null && distortionCoeffs != null) {

            val dCoeffMatOfDouble = MatOfDouble()
            val distArray = DoubleArray(distortionCoeffs.rows()) { idx ->
                distortionCoeffs.get(idx, 0)[0]
            }
            dCoeffMatOfDouble.fromArray(*distArray)

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
                Calib3d.solvePnP(objectPoints, imagePoints, cameraMatrix, dCoeffMatOfDouble, rvec, tvec)
                markerTvecs.add(Pair(markerId, tvec))
                markerRvecs.add(Pair(markerId, rvec))

                val points = MatOfPoint(
                    Point(corner.get(0, 0)[0], corner.get(0, 0)[1]),
                    Point(corner.get(0, 1)[0], corner.get(0, 1)[1]),
                    Point(corner.get(0, 2)[0], corner.get(0, 2)[1]),
                    Point(corner.get(0, 3)[0], corner.get(0, 3)[1])
                )
                val thickness = 4;

                var outlineColor = GOOD_SCALAR
                if (i == rectangeConfidence.culpritMarkerIndex) {
                    outlineColor = WARN_SCALAR
                }

                Imgproc.polylines(rgba, listOf(points), true, outlineColor, thickness)

            }

            markerConfidences = calculateConfidence(corners, ids, markerRvecs,markerTvecs, cameraMatrix, dCoeffMatOfDouble, 0.05 )
//            var formattedWidth: String
//            var formattedHeight: String


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
                        formatHeight = decimalTo32nds(rounded).toString()
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
                        formatWidth = decimalTo32nds(rounded).toString()
                        width = "%.4f".format(rounded).toDouble()
                    }
                    lineIndex++
                }
            }
        }
        val tmpData: ProcessorInfo = object: ProcessorInfo {
            override var currentWidth: String = formatWidth
            override var currentHeight: String = formatHeight
            override var squareConfidence: Double = squareConfidences
            override var markerConfidence: Map<Int, Double> = markerConfidences
        }
        return tmpData
//        return Pair(width, height)
    }

    fun decimalTo32nds(inches: Double): String {
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
