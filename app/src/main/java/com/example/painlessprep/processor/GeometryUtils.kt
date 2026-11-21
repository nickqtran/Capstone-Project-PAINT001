package com.example.painlessprep.processor

import org.opencv.core.Mat
import org.opencv.core.Point
import java.lang.Math.toDegrees
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

data class RectangeConfidence(
    val confidence: Double,
    val statusMessage: String,
    val culpritMarkerIndex: Int? // Index (0, 1, or 2) of the marker causing the issue
)

object GeometryUtils {
    /**
     * Validates the rectangle layout and identifies which specific marker is causing a geometric failure.
     */
    fun diagnoseRectangleLayout(
        corners: List<Mat>,
        ids: Mat
    ): RectangeConfidence {
        if (ids.empty() || corners.isEmpty() || ids.rows() < 3) {
            return RectangeConfidence(0.0, "Insufficient markers detected (Need exactly 3)", null)
        }

        // 1. Safely extract 2D pixel coordinates for the 3 markers
        val centers = ArrayList<Point>()
        val markerIds = ArrayList<Int>()
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
            markerIds.add(ids.get(i, 0)[0].toInt())
        }

        if (centers.size < 3) {
            return RectangeConfidence(0.0, "Failed to parse marker coordinates safely", null)
        }

        val p0 = centers[0]
        val p1 = centers[1]
        val p2 = centers[2]

        // 2. Calculate the lengths of all three connecting side segments
        val side01 = sqrt((p1.x - p0.x).pow(2) + (p1.y - p0.y).pow(2))
        val side12 = sqrt((p2.x - p1.x).pow(2) + (p2.y - p1.y).pow(2))
        val side20 = sqrt((p0.x - p2.x).pow(2) + (p0.y - p2.y).pow(2))

        // 3. Calculate true interior angles at each marker vertex using the Law of Cosines
        val angle0 = toDegrees(acos(max(-1.0, min(1.0, (side01.pow(2) + side20.pow(2) - side12.pow(2)) / (2.0 * side01 * side20)))))
        val angle1 = toDegrees(acos(max(-1.0, min(1.0, (side01.pow(2) + side12.pow(2) - side20.pow(2)) / (2.0 * side01 * side12)))))
        val angle2 = toDegrees(acos(max(-1.0, min(1.0, (side12.pow(2) + side20.pow(2) - side01.pow(2)) / (2.0 * side12 * side20)))))

        // 4. In a right triangle, the longest side is the hypotenuse. The opposite angle MUST be 90°.
        val maxSide = max(side01, max(side12, side20))

        val expectedRightAngle: Double
        val cornerMarkerIdx: Int
        val endpointIdx1: Int
        val endpointIdx2: Int

        when (maxSide) {
            side12 -> { // Side between Marker 1 and 2 is the longest -> Marker 0 should be the 90° corner
                expectedRightAngle = angle0
                cornerMarkerIdx = 0; endpointIdx1 = 1; endpointIdx2 = 2
            }
            side20 -> { // Side between Marker 2 and 0 is the longest -> Marker 1 should be the 90° corner
                expectedRightAngle = angle1
                cornerMarkerIdx = 1; endpointIdx1 = 0; endpointIdx2 = 2
            }
            else -> {   // Side between Marker 0 and 1 is the longest -> Marker 2 should be the 90° corner
                expectedRightAngle = angle2
                cornerMarkerIdx = 2; endpointIdx1 = 0; endpointIdx2 = 1
            }
        }

        // 5. Check if points are arranged in a flat straight line (collinear)
        if (expectedRightAngle > 165.0 || expectedRightAngle < 15.0) {
            return RectangeConfidence(0.0, "Markers are aligned in a straight line. Culprit: Marker ID ${markerIds[cornerMarkerIdx]}", cornerMarkerIdx)
        }

        // 6. Score the corner error (allowing up to 25 degrees variance for perspective distortion)
        val angleError = abs(90.0 - expectedRightAngle)
        val confidence = max(0.0, 1.0 - (angleError / 25.0))
        val finalConfidence = Math.round(confidence * 100.0) / 100.0

        // 7. Determine the layout failure culprit if confidence drops below threshold
        if (finalConfidence < 0.85) {
            // If the right angle is obtuse (>90), the corner vertex is pushed too far out
            // If it is acute (<90), the endpoints are tracking too wide relative to the frame.
            return if (expectedRightAngle > 105.0) {
                RectangeConfidence(finalConfidence, "Angle too wide at corner (${Math.round(expectedRightAngle)}°). Culprit: Marker ID ${markerIds[cornerMarkerIdx]}", cornerMarkerIdx)
            } else {
                // Find which side is disproportionately long causing the distortion
                val leg1 = sqrt((centers[endpointIdx1].x - centers[cornerMarkerIdx].x).pow(2) + (centers[endpointIdx1].y - centers[cornerMarkerIdx].y).pow(2))
                val leg2 = sqrt((centers[endpointIdx2].x - centers[cornerMarkerIdx].x).pow(2) + (centers[endpointIdx2].y - centers[cornerMarkerIdx].y).pow(2))

                val badEndpoint = if (leg1 > leg2) endpointIdx1 else endpointIdx2
                RectangeConfidence(finalConfidence, "Asymmetric skew detected. Drift culprit: Marker ID ${markerIds[badEndpoint]}", badEndpoint)
            }
        }

        return RectangeConfidence(finalConfidence, "Valid rectangle layout", null)
    }

}