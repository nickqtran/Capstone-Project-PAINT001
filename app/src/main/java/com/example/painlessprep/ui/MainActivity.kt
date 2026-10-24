package com.example.painlessprep.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.InputType
import android.view.SurfaceView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.painlessprep.R
import com.example.painlessprep.model.WindowData
import com.example.painlessprep.processor.OpenCVProcessor
import com.example.painlessprep.viewmodel.MainViewModel
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.JavaCameraView
import org.opencv.android.OpenCVLoader
import org.opencv.calib3d.Calib3d
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.DetectorParameters
import org.opencv.objdetect.Dictionary
import org.opencv.objdetect.Objdetect
import org.opencv.objdetect.ArucoDetector
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * The main activity of Pane Perfect, handles UI rendering, camera preview, and user interactions.
 */
class MainActivity : AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2 {

    private lateinit var cameraView: JavaCameraView
    private lateinit var arucoDetector: ArucoDetector
    private lateinit var dictionary: Dictionary
    private lateinit var parameters: DetectorParameters

    private val viewModel: MainViewModel by viewModels()
    private val openCVProcessor = OpenCVProcessor()

    // Chessboard Square size (~22mm per square)
    private val calibSquareSize = 0.022
    private val requiredFrames = 30
    private val boardSize = Size(9.0, 6.0)

    // Used for calibration delay
    private var lastCaptureTime = 0L

    // Global distance measurements each frame
    private var idHeight: Double = 0.0
    private var idWidth: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize OpenCV
        if (!OpenCVLoader.initDebug()) {
            println("OpenCV failed to load")
        } else {
            println("OpenCV loaded successfully")
        }

        dictionary = Objdetect.getPredefinedDictionary(Objdetect.DICT_4X4_50)
        parameters = DetectorParameters()
        arucoDetector = ArucoDetector(dictionary, parameters)

        setContentView(R.layout.activity_main)

        cameraView = findViewById(R.id.camera_view)
        cameraView.visibility = SurfaceView.VISIBLE
        cameraView.setCvCameraViewListener(this)

        // Observe RMS display from ViewModel
        viewModel.rmsDisplay.observe(this) { rms ->
            updateRmsDisplay(rms)
        }

        // Check camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)
        } else {
            cameraView.setCameraPermissionGranted()
            cameraView.enableView()
        }

        findViewById<Button>(R.id.btn_calibrate).setOnClickListener {
            promptCalibration()
        }

        findViewById<Button>(R.id.btn_capture).setOnClickListener {
            captureMeasurement(idWidth, idHeight)
        }

        findViewById<ImageButton>(R.id.btn_infocard).setOnClickListener {
            showInfoCard()
        }
    }

    override fun onResume() {
        super.onResume()
        if (OpenCVLoader.initDebug()) {
            cameraView.setCameraPermissionGranted()
            cameraView.enableView()
        }
    }

    override fun onPause() {
        super.onPause()
        cameraView.disableView()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraView.disableView()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            cameraView.setCameraPermissionGranted()
            cameraView.enableView()
        }
    }

    override fun onCameraViewStarted(width: Int, height: Int) {}

    override fun onCameraViewStopped() {}

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        val rgba = inputFrame.rgba()
        val gray = inputFrame.gray()
        var cameraMatrix: Mat? = null
        var distortionCoeffs: Mat? = null

        val savedCalib = viewModel.savedCalibration
        if (savedCalib != null) {
            cameraMatrix = savedCalib.cameraMatrix
            distortionCoeffs = savedCalib.distortionCoeffs
        }

        if (viewModel.isCalibrating && !viewModel.isProcessingCalibration) {
            val currentTime = System.currentTimeMillis()
            val corners = MatOfPoint2f()
            val foundBoard = Calib3d.findChessboardCorners(
                gray, boardSize, corners,
                Calib3d.CALIB_CB_ADAPTIVE_THRESH or Calib3d.CALIB_CB_NORMALIZE_IMAGE
            )

            if (foundBoard && currentTime - lastCaptureTime > 1000) {
                lastCaptureTime = currentTime

                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Chessboard Found, (${viewModel.collectedObjectPoints.size + 1}/$requiredFrames)",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                Imgproc.cornerSubPix(
                    gray, corners,
                    Size(11.0, 11.0),
                    Size(-1.0, -1.0),
                    TermCriteria(TermCriteria.EPS + TermCriteria.MAX_ITER, 50, 0.001)
                )

                viewModel.collectedImagePoints.add(corners)

                val objPoints = MatOfPoint3f()
                val points = mutableListOf<Point3>()
                for (i in 0 until boardSize.height.toInt()) {
                    for (j in 0 until boardSize.width.toInt()) {
                        points.add(Point3(j * calibSquareSize, i * calibSquareSize, 0.0))
                    }
                }
                objPoints.fromList(points)
                viewModel.collectedObjectPoints.add(objPoints)

                Calib3d.drawChessboardCorners(rgba, boardSize, corners, foundBoard)

                if (viewModel.collectedObjectPoints.size >= requiredFrames && !viewModel.isProcessingCalibration) {
                    viewModel.isCalibrating = false
                    viewModel.isProcessingCalibration = true
                    Thread {
                        runCalibrationTask(rgba.size())
                    }.start()
                }
            }
        } else {
            val corners = ArrayList<Mat>()
            val ids = Mat()
            arucoDetector.detectMarkers(gray, corners, ids)

            val markerTvecs = mutableListOf<Pair<Int, Mat>>()

            if (!ids.empty()) {
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

                    if (distortionCoeffs != null && cameraMatrix != null) {
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
                }

                if (distortionCoeffs != null) {
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
                                idHeight = "%.4f".format(rounded).toDouble()
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
                                idWidth = "%.4f".format(rounded).toDouble()
                            }
                            lineIndex++
                        }
                    }
                }
            }
        }
        return rgba
    }

    private fun runCalibrationTask(imageSize: Size) {
        val objectPointCast = viewModel.collectedObjectPoints.map { it as Mat }
        val imagePointCast = viewModel.collectedImagePoints.map { it as Mat }

        val cameraMatrix = Mat.zeros(3, 3, CvType.CV_64F)
        val distortionCoeffs = Mat.zeros(5, 1, CvType.CV_64F)
        val rvecs = mutableListOf<Mat>()
        val tvecs = mutableListOf<Mat>()

        val rms = openCVProcessor.runCalibration(
            objectPointCast,
            imagePointCast,
            imageSize,
            cameraMatrix,
            distortionCoeffs,
            rvecs,
            tvecs
        )

        runOnUiThread {
            Toast.makeText(this, "Calibration Finished! RMS ERROR: $rms", Toast.LENGTH_LONG).show()
        }

        viewModel.saveNewCalibration(cameraMatrix, distortionCoeffs, rms)
        viewModel.isProcessingCalibration = false
    }

    private fun promptCalibration() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Calibration Instructions")
            .setMessage(
                "To calibrate your device, please move the camera around the provided chessboard, " +
                        "try to get many angles and different orientations of the board. " +
                        "The device will alert you when finished!"
            )
            .setPositiveButton("I understand") { _, _ ->
                viewModel.isCalibrating = true
                viewModel.collectedObjectPoints.clear()
                viewModel.collectedImagePoints.clear()
                Toast.makeText(
                    this,
                    "Beginning calibration, please keep a chessboard in camera view and move around!",
                    Toast.LENGTH_LONG
                ).show()
            }
            .setNegativeButton("Cancel") { _, _ ->
                viewModel.isCalibrating = false
                Toast.makeText(this, "Calibration ended, please retry.", Toast.LENGTH_LONG).show()
            }
        builder.create().show()
    }

    private fun showInfoCard() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Using The App")
            .setMessage(
                "Here is a step by step guide in using the application.\n\n" +
                        "Step 1) Calibrate your mobile camera by placing the provided chessboard in the frame...\n\n" +
                        "Step 2) Place ArUco markers in corners.\n\n" +
                        "Step 3) Position in front of window.\n\n" +
                        "Step 4) Hit Capture to save measurements.\n\n" +
                        "Step 5) Confirm room info."
                    )
            .setPositiveButton("Got it!") { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    private fun updateRmsDisplay(rms: Double) {
        val textView = findViewById<TextView>(R.id.rmsText)
        val formattedText = String.format("RMS: %.3f", rms)
        runOnUiThread {
            textView.text = formattedText
        }
    }

    private fun captureMeasurement(widthVal: Double, heightVal: Double) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val textEntry = EditText(this).apply { hint = "Enter Window Name.." }
        val intEntry = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            hint = "Enter Window Amount Number.."
        }

        layout.addView(textEntry)
        layout.addView(intEntry)

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Measurement Details")
            .setMessage("The following measurement data will be saved: \nWidth: $widthVal\nHeight: $heightVal\n")
            .setView(layout)
            .setPositiveButton("Confirm") { _, _ ->
                val name = textEntry.text.toString()
                val amtEntry = intEntry.text.toString()

                when {
                    name.isEmpty() && amtEntry.isEmpty() -> Toast.makeText(this, "Measurement name and amount required.", Toast.LENGTH_LONG).show()
                    name.isEmpty() -> Toast.makeText(this, "Measurement name required.", Toast.LENGTH_LONG).show()
                    amtEntry.isEmpty() -> Toast.makeText(this, "Window amount required.", Toast.LENGTH_LONG).show()
                    else -> {
                        val amount = amtEntry.toInt()
                        val windowData = WindowData(name, widthVal, heightVal, amount)
                        viewModel.saveMeasurement(windowData)
                        Toast.makeText(this, "Measurement '$name' saved!", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                Toast.makeText(this, "Measurement cancelled, please try again!", Toast.LENGTH_LONG).show()
            }
        builder.create().show()
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
