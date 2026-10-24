package com.example.painlessprep.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.InputType
import android.view.SurfaceView
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.painlessprep.model.WindowData
import com.example.painlessprep.processor.OpenCVProcessor
import com.example.painlessprep.viewmodel.MainViewModel
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.JavaCameraView
import org.opencv.android.OpenCVLoader
import org.opencv.core.*

class MainActivity : AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2 {

    private var cameraView: JavaCameraView? = null
    private val viewModel: MainViewModel by viewModels()
    private val openCVProcessor = OpenCVProcessor()

    private val requiredFrames = 30
    private val calibSquareSize = 0.022
    private val boardSize = Size(9.0, 6.0)
    private var lastCaptureTime = 0L
    private var lastCvProcessTime = 0L
    private val cvProcessIntervalMs = 66L // ~15fps CV throttling

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!OpenCVLoader.initLocal()) {
            println("OpenCV failed to load")
        } else {
            println("OpenCV loaded successfully")
        }

        setContent {
            CameraScreen(
                viewModel = viewModel,
                onCalibrateClicked = { promptCalibration() },
                onCaptureClicked = { captureMeasurement(viewModel.currentWidth, viewModel.currentHeight) },
                onInfoClicked = { showInfoCard() },
                cameraViewFactory = { context ->
                    JavaCameraView(context, 0).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        visibility = SurfaceView.VISIBLE
                        setCvCameraViewListener(this@MainActivity)
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED
                        ) {
                            setCameraPermissionGranted()
                            enableView()
                        }
                    }.also { cameraView = it }
                }
            )
        }

        checkCameraPermission()
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)
        }
    }

    override fun onResume() {
        super.onResume()
        if (OpenCVLoader.initLocal()) {
            cameraView?.setCameraPermissionGranted()
            cameraView?.enableView()
        }
    }

    override fun onPause() {
        super.onPause()
        cameraView?.disableView()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraView?.disableView()
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
            cameraView?.setCameraPermissionGranted()
            cameraView?.enableView()
        }
    }

    override fun onCameraViewStarted(width: Int, height: Int) {}
    override fun onCameraViewStopped() {}

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        val rgba = inputFrame.rgba()
        val gray = inputFrame.gray()
        val currentTime = System.currentTimeMillis()

        val savedCalib = viewModel.savedCalibration
        val cameraMatrix = savedCalib?.cameraMatrix
        val distortionCoeffs = savedCalib?.distortionCoeffs

        if (viewModel.isCalibrating && !viewModel.isProcessingCalibration) {
            if (currentTime - lastCvProcessTime >= cvProcessIntervalMs) {
                lastCvProcessTime = currentTime
                val corners = openCVProcessor.processCalibrationFrame(gray, rgba, boardSize, calibSquareSize)

                if (corners != null && currentTime - lastCaptureTime > 1000) {
                    lastCaptureTime = currentTime

                    runOnUiThread {
                        Toast.makeText(
                            this,
                            "Chessboard Found, (${viewModel.collectedObjectPoints.size + 1}/$requiredFrames)",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

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

                    if (viewModel.collectedObjectPoints.size >= requiredFrames && !viewModel.isProcessingCalibration) {
                        viewModel.isCalibrating = false
                        viewModel.isProcessingCalibration = true
                        Thread {
                            runCalibrationTask(rgba.size())
                        }.start()
                    }
                }
            }
        } else {
            if (currentTime - lastCvProcessTime >= cvProcessIntervalMs) {
                lastCvProcessTime = currentTime
                val (width, height) = openCVProcessor.processArucoFrame(gray, rgba, cameraMatrix, distortionCoeffs)
                viewModel.currentWidth = width
                viewModel.currentHeight = height
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
        builder.setTitle("TEST: Using The App")
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
}
