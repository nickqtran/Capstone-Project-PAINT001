package com.example.painlessprep

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.InputType
import android.view.SurfaceView
import android.view.Window
import android.widget.Toast
import kotlin.math.roundToInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import org.json.JSONArray
import org.json.JSONObject
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.JavaCameraView
import org.opencv.android.OpenCVLoader
import org.opencv.calib3d.Calib3d
import org.opencv.core.Mat
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.ArucoDetector
import org.opencv.objdetect.Dictionary
import org.opencv.objdetect.Objdetect
import org.opencv.objdetect.DetectorParameters
import android.widget.ImageButton
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import java.io.File
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt
import com.example.painlessprep.CsvUtils


// Main activity implements OpenCV camera
/**
 * The main class of Pane Perfect, contains all functions used in the application.
 */
class MainActivity : AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2 {


    private lateinit var cameraView: JavaCameraView
    private lateinit var arucoDetector: ArucoDetector
    private lateinit var dictionary: Dictionary
    private lateinit var parameters: DetectorParameters
    private var savedCalibration: CalibrationData? = null

    //Boolean to check if we want to calibrate
    var isCalibrating = false
    //Boolean check to see if we are currently calibrating
    var isProcessingCalibration = false
    //Chessboard Square size (mine printed out to ~22mm per square
    val calibSquareSize = .022 //22MM
    //Amount of frames to take when we calibrate, 20-30 if good practice for calibration
    val requiredFrames = 30
    //The size of the chessboard, mine is 10x7 squares, which means its a 9x6 chessboard
    val boardSize = Size(9.0,6.0)

    //Our lists that store our calibration data.
    val collectedImagePoints = mutableListOf<MatOfPoint2f>()
    val collectedObjectPoints = mutableListOf<MatOfPoint3f>()

    //used for calibration delay
    var lastCaptureTime = 0L

    //our global distance measurements each frame
    var idHeight : Double = 0.0 //0 to 1 distance
    var idWidth : Double = 0.0 //1 to 2 distance


    /**
     * The function called on the initial load of the application.
     */
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
        savedCalibration = loadCalibration()

        // Set the UI layout
        setContentView(R.layout.activity_main)

        // Link the camera view from XML layout
        cameraView = findViewById(R.id.camera_view)

        //Update the rms display if we have loaded calibration data
        val calibData = savedCalibration
        if(calibData != null) {
            updateRmsDisplay(calibData.rms)
        }

        // Make sure the camera view is visible
        cameraView.visibility = SurfaceView.VISIBLE


        cameraView.setCvCameraViewListener(this)

        // Check if camera permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permission if not granted
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)
        } else {
            // If permission already granted, enable camera
            cameraView.setCameraPermissionGranted()
            cameraView.enableView()
        }

        val btnCalibrate = findViewById<Button>(R.id.btn_calibrate)
        btnCalibrate.setOnClickListener {
            //prompt the calibration instructions to check if we are calibrating
            promptCalibration()

        }

        val btnCapture = findViewById<Button>(R.id.btn_capture)
        btnCapture.setOnClickListener {
            captureMeasurement(idWidth,idHeight)
        }

        val btnInfocard = findViewById<ImageButton>(R.id.btn_infocard)
        btnInfocard.setOnClickListener {
            showInfoCard()
        }

    }

    /**
     * The function called when focusing back onto the application from an unfocused view.
     */
    override fun onResume() {
        super.onResume()

        // Re-initialize OpenCV when returning to app
        if (OpenCVLoader.initDebug()) {

            cameraView.setCameraPermissionGranted()
            cameraView.enableView()

        }
    }

    /**
     * The function called when un-focusing from view. (Tabbing out)
     */
    override fun onPause() {
        super.onPause()

        // Disable camera when app is paused
        cameraView.disableView()
    }

    /**
     * Function called to kill the application properly.
     */
    override fun onDestroy() {
        super.onDestroy()

        // Release camera when activity is destroyed
        cameraView.disableView()
    }

    /**
     * Function called to handle the permission request popup.
     */
    // Handle result of permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // If permission granted, enable camera
        if (requestCode == 1 &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            cameraView.setCameraPermissionGranted()
            cameraView.enableView()
        }
    }


    // Called when camera starts
    override fun onCameraViewStarted(width: Int, height: Int) {
    }

    // Called when camera stops
    override fun onCameraViewStopped() {
        // Cleanup resources here
    }


    /**
     * Main function called every "frame", essentially called ~30 times per second.
     * This function is also where we check if we are calibrating, or writing aruco data to the screen.
     *
     * @param[inputFrame] The given frame returned by the OpenCV camera view.
     *
     * @return An OpenCV matrix consisting of the camera frame data.
     */
    // Called for every camera frame
    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        val rgba = inputFrame.rgba()
        val gray = inputFrame.gray()
        var cameraMatrix: Mat? = null
        var distortionCoeffs: Mat? = null
        var id01 = 0.0
        var id12 = 0.0




        if(savedCalibration != null) {
            val (cMat, dCoeffs) = savedCalibration!!
            cameraMatrix = cMat
            distortionCoeffs = dCoeffs
        }

        //run calibration code if we are in calibration mode, make sure we arent already in the process of calibrating
        if (isCalibrating && !isProcessingCalibration) {
            //grab out current time to know if we should capture a frame
            val currentTime = System.currentTimeMillis()
            //start by creating an array for the corners, and search for a chessboard
            val corners = MatOfPoint2f()
            val foundBoard = Calib3d.findChessboardCorners(
                gray, boardSize, corners,
                Calib3d.CALIB_CB_ADAPTIVE_THRESH or
                        Calib3d.CALIB_CB_NORMALIZE_IMAGE)

            if (foundBoard && currentTime - lastCaptureTime > 1000) {
                //if we have found a board, and we are past the time threshold (0.33 fps)
                lastCaptureTime = currentTime

                //Let the user know we've found a chessboard, and how many frames are left to capture
                runOnUiThread {
                    Toast.makeText(this, "Chessboard Found, (${collectedObjectPoints.size + 1}/$requiredFrames)", Toast.LENGTH_SHORT).show()
                }
                //if we find a chessboard, refine the corners for more accuracy
                Imgproc.cornerSubPix(
                    gray,
                    corners,
                    Size(11.0, 11.0),
                    Size(-1.0, -1.0),
                    TermCriteria(TermCriteria.EPS + TermCriteria.MAX_ITER, 50, 0.001)
                )
                //Based off that refinement, add those  points to collected image points
                collectedImagePoints.add(corners)

                //Now, we generate 3D object points
                val objPoints = MatOfPoint3f()
                val points = mutableListOf<Point3>()
                //loop through the board size to fill points
                for (i in 0 until boardSize.height.toInt()) {
                    for (j in 0 until boardSize.width.toInt()) {
                        points.add(Point3(j * calibSquareSize, i * calibSquareSize, 0.0))
                    }
                }
                //Add the found points to our object points list
                objPoints.fromList(points)
                collectedObjectPoints.add(objPoints)

                //draw the board on our frame
                Calib3d.drawChessboardCorners(rgba, boardSize, corners, foundBoard)

                //Start a secondary thread to run calibration (supposedly should lighten the load)
                if (collectedObjectPoints.size >= requiredFrames && !isProcessingCalibration) {
                    isCalibrating = false
                    isProcessingCalibration = true
                    Thread {
                        runCalibration(rgba.size())
                    }.start()


                }

            }



        } else { //If we aren't calibrating, run the aruco detection code!
            val corners = ArrayList<Mat>()
            val ids = Mat()

            // Detect markers
            arucoDetector.detectMarkers(gray, corners, ids)

            //we need a mat pair to hold id-tvec relations
            val markerTvecs = mutableListOf<Pair<Int, Mat>>()

            //Run solvePNP code
            if(!ids.empty()) {
                for ( i in corners.indices) {
                    val corner = corners[i]
                    //grab our marker id
                    val markerId = ids[i,0][0].toInt()

                    //Create our imagePoints matrix, just uses the corners array values
                    val pts = corner.reshape(2, 4)
                    val imagePoints = MatOfPoint2f(pts)

                    //Now we need to define our known marker sizes for proper estimation
                    //2inch = 0.05
                    //2.5inch = 0.064
                    //3inch = 0.076
                    val markerSize = 0.05 //2inch converted to meters

                    //now for our objectPoints matrix, this contains the markers coordinates to be changed

                    val halfSize = markerSize / 2.0

                    val objectPoints = MatOfPoint3f(
                        Point3(-halfSize,  halfSize, 0.0),
                        Point3( halfSize,  halfSize, 0.0),
                        Point3( halfSize, -halfSize, 0.0),
                        Point3(-halfSize, -halfSize, 0.0)
                    )

                    //we now create our tvec and rvec matrixes to be written to later
                    val rvec = Mat()
                    val tvec = Mat()

                    //We need one last thing, to convert our distortion coeff to a matofdouble,
                    //since its nullable we must check it's not null
                    if(distortionCoeffs != null) {
                        val dCoeffMatOfDouble = MatOfDouble()
                        val distArray = DoubleArray(distortionCoeffs.rows()) {
                                i -> distortionCoeffs.get(i,0)[0]
                        }
                        dCoeffMatOfDouble.fromArray(*distArray)

                        //And now, using the data collected, we can run SolvePNP to compute our rvec and tvec
                        Calib3d.solvePnP(objectPoints, imagePoints, cameraMatrix, dCoeffMatOfDouble, rvec, tvec)

                        markerTvecs.add(Pair(markerId, tvec))

                        //Now, given the results from solvePNP, we can calculate distances
                        val distanceFromCamera = sqrt(
                            //grab the x,y, and z of our tvec to calculate the distance from the camera
                            tvec.get(0, 0)[0].pow(2) +
                                    tvec.get(1, 0)[0].pow(2) +
                                    tvec.get(2, 0)[0].pow(2)
                        )

                        //from here, we can now begin to draw outlines, and start to get distance between markers
                        val points = MatOfPoint (
                            Point(corner.get(0,0)[0], corner.get(0,0)[1]),
                            Point(corner.get(0,1)[0], corner.get(0,1)[1]),
                            Point(corner.get(0,2)[0], corner.get(0,2)[1]),
                            Point(corner.get(0,3)[0], corner.get(0,3)[1])
                        )
                        Imgproc.polylines(rgba, listOf(points), true, Scalar(0.0,255.0,0.0,3.0))

                        //Next we draw the marker ID and distance from the camera onto the frame (cam dist not necessary for display but neat)
                        val centerX = (corner.get(0,0)[0] + corner.get(0,2)[0])/2
                        val centerY = (corner.get(0,0)[1] + corner.get(0,2)[1])/2

                        //Write the data on the markers
                        Imgproc.putText(
                            rgba,
                            "ID: $markerId : Dist: %.2f m".format(distanceFromCamera),
                            Point(centerX,centerY),
                            Imgproc.FONT_HERSHEY_SIMPLEX,
                            1.0,
                            Scalar(255.0,0.0,0.0),
                            2
                        )


                    }



                }

                if(distortionCoeffs != null) {
                    var lineIndex = 0


                    for (i in 0 until markerTvecs.size) {
                        for (j in i + 1 until markerTvecs.size) {

                            val (id1, tvec1) = markerTvecs[i]
                            val (id2, tvec2) = markerTvecs[j]

                            //if the two ids are the hypotenuse, skip
                            if((id1 == 0 && id2 == 2) || (id1 == 2 && id2 == 0)) continue

                            //we obtain our measurements through finding the difference in coordinate values
                            val dx = tvec2.get(0,0)[0] - tvec1.get(0,0)[0]
                            val dy = tvec2.get(1,0)[0] - tvec1.get(1,0)[0]
                            val dz = tvec2.get(2,0)[0] - tvec1.get(2,0)[0]

                            //then take the square root of each value squared and summed,
                            //multiplied to convert to inches,and add 2 inches to account for marker size
                            //We then add the marker size as the algorithm measures center to center, essentially losing 1 marker of distance from edge to edge
                            //We then also add the whitespace border for the same reason.
                            //These values are hard coded. if they change, we need to change them here.
                            val markerDistance = (sqrt(dx*dx + dy*dy + dz*dz) * 39.37) + (2 + (0.25*2))
                            val rounded = kotlin.math.round(markerDistance * 32) / 32 //round to the nearest 32nd of an inch




                            if((id1 == 0 && id2 == 1) || (id1 == 1 && id2 == 0)) {
                                //Display the measurement results to the 32nd, or 4 decimal places
                                Imgproc.putText(
                                    rgba,
                                    "Height: ${decimalTo32nds(rounded)}",
                                    Point(50.0, 50.0 + (30.0 * lineIndex)),
                                    Imgproc.FONT_HERSHEY_SIMPLEX,
                                    0.8,
                                    Scalar(5.0, 252.0, 244.0),
                                    2
                                )

                                // 0-1 measurement write
                                idHeight = "%.4f".format(rounded).toDouble()
                            } else if ((id1 == 1 && id2 == 2) || (id1 == 2 && id2 == 1)) {
                                //Display the measurement results to the 32nd, or 4 decimal places
                                Imgproc.putText(
                                    rgba,
                                    "Width: ${decimalTo32nds(rounded)}",
                                    Point(50.0, 50.0 + (30.0 * lineIndex)),
                                    Imgproc.FONT_HERSHEY_SIMPLEX,
                                    0.8,
                                    Scalar(5.0, 252.0, 244.0),
                                    2
                                )
                                //1-2 check

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


    /**
     * The function that performs camera calibration
     *
     * @param[imageSize] An OpenCV size object, representing the image width and height
     */
    //Function that actually runs the calibration
    fun runCalibration(imageSize : Size) {

        //Cast the image and object points as standard opencv mats
        val objectPointCast = collectedObjectPoints.map { it as Mat }
        val imagePointCast = collectedImagePoints.map { it as Mat }


        val cameraMatrix = Mat.zeros(3, 3, CvType.CV_64F)
        val distortionCoeffs = Mat.zeros(5, 1, CvType.CV_64F)
        val rvecs = mutableListOf<Mat>()
        val tvecs = mutableListOf<Mat>()

        //Run the calibration using
        val flags = Calib3d.CALIB_RATIONAL_MODEL

        val rms = Calib3d.calibrateCamera(
            objectPointCast,
            imagePointCast,
            imageSize,
            cameraMatrix,
            distortionCoeffs,
            rvecs,
            tvecs,
            flags
        )
        runOnUiThread {
            Toast.makeText(this, "Calibration Finished! RMS ERROR: $rms", Toast.LENGTH_LONG).show()
        }
        updateRmsDisplay(rms)

        //save our calibration data for future use
        saveCalibration(cameraMatrix, distortionCoeffs, rms)

        isProcessingCalibration = false
    }


    /**
     * The function that saves a cameras calibration data to a devices storage for later use.
     *
     * @param[cameraMatrix] An OpenCV matrix for storing the camera data to.
     * @param[distortionCoeffs] An OpenCV matrix for storing the camera distortion coefficients to.
     * @param[rms] A double to store the calibration error value to.
     */
    //Function to save calibration data
    fun saveCalibration(cameraMatrix: Mat, distortionCoeffs: Mat, rms: Double ) {
        //grab our current preference data
        val prefs = getSharedPreferences("CameraPrefs", MODE_PRIVATE)

        //fill cameraArray with our camera matrix
        val cameraArray = DoubleArray(9)
        cameraMatrix.get(0,0, cameraArray)

        //fill distArray with our distance coeffecients
        val distArray = DoubleArray(distortionCoeffs.total().toInt())
        distortionCoeffs.get(0,0,distArray)

        val rmsVal = rms

        //Crete our JSON object that will act as our storage
        val json = JSONObject()
        json.put("cameraMatrix", JSONArray(cameraArray.toList()))
        json.put("distanceCoeffs", JSONArray(distArray.toList()))
        json.put("rmsValue", rmsVal)


        prefs.edit().putString("calibration", json.toString()).apply()
    }

    /**
     * The fucntion utilized to load calibration data on application opening.
     *
     * @return A nullable CalibrationData data object storing the loaded json data.
     */
    fun loadCalibration(): CalibrationData? {
        //dig into the preferences and grab our calibration preferences
        val prefs = getSharedPreferences("CameraPrefs", MODE_PRIVATE)
        val jsonString = prefs.getString("calibration", null) ?: return null
        val json = JSONObject(jsonString)

        //Load the camera matrix data
        val cameraArray = json.getJSONArray("cameraMatrix")
        val cameraMatrix = Mat(3,3, CvType.CV_64F)
        for( i in 0 until 3) {
            for (j in 0 until 3) {
                cameraMatrix.put(i , j , cameraArray.getDouble(i * 3 + j))
            }
        }

        //Load the distance coefficients data
        val distArray = json.getJSONArray("distanceCoeffs")
        val distortionCoeffs = Mat(distArray.length(), 1, CvType.CV_64F)
        for(i in 0 until distArray.length()) {
            distortionCoeffs.put(i, 0, distArray.getDouble(i))
        }

        //Load the RMS error value of the saved data
        val rms = json.getDouble("rmsValue")

        //return the data for usage
        return CalibrationData(cameraMatrix, distortionCoeffs, rms)

    }

    /**
     * Allows displaying of a prompt before calibration starts.
     * This prompt will instruct the user on how to best calibrate their device for measuring.
     * This utilizes the AlertDialog class from androidx
     */
    fun promptCalibration() {
        //create the alert dialog builder
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)

        //set the dialog text/titles
        builder
            .setTitle("Calibration Instructions")
            .setMessage("To calibrate your device, " +
                        "please move the camera around the provided chessboard," +
                        "try to get many angles and different orientations of the board." +
                        "The device will alert you when finished!")
            .setPositiveButton("I understand") { dialog, which ->
                isCalibrating = true
                collectedObjectPoints.clear()
                collectedImagePoints.clear()

                Toast.makeText(this,"Beginning calibration, please keep a chessboard in camera view and move around!", Toast.LENGTH_LONG).show()
            }
            .setNegativeButton("Cancel") { dialog, which ->
                isCalibrating = false
                Toast.makeText(this,"Calibration ended, please retry.", Toast.LENGTH_LONG).show()
            }

        //Create our dialog alert
        val dialog: AlertDialog = builder.create()
        dialog.show()

    }

    /**
     * Displays usage information
     *
     */

    fun showInfoCard() {
        val builder = AlertDialog.Builder(this)

        builder
            .setTitle("Using The App")
            .setMessage(
                "Here is a step by step guide " +
                "in using the application.\n\n" +
                "Step 1) Calibrate your mobile " +
                "camera by placing the provided " +
                "chessboard in the frame. " +
                "Then, hit the calibration button " +
                "and move your phone around. " +
                "(If prompted, you may need to " +
                "allow camera permissions.) " +
                "Try to capture as many angles as " +
                "possible during calibration.\n\n"  +
                "Step 2) Now that your camera " +
                "is calibrated, place the provided " +
                "ArUco markers in 3 corners of the " +
                "window you would like to measure.\n\n" +
                "Step 3) After placing the ArUco " +
                "markers, position yourself in front " +
                "of the window and ensure the window " +
                "is filling the camera view on the app.\n\n" +
                "Step 4) Once the height and width " +
                "are calculated, hit the Capture button " +
                "to capture the measurements.\n\n" +
                "Step 5) Input the room information " +
                "and hit Confirm to save your " +
                "measurements."
            )
            .setPositiveButton("Got it!") {dialog, _ ->
                dialog.dismiss()

            }

        builder.create().show()
    }

    /**
     * Updates the RMS error display value
     *
     * @param[rms] A double representing the RMS error returned when calibrating.
     */
    fun updateRmsDisplay(rms: Double) {
        val textView = findViewById<TextView>(R.id.rmsText)

        val formattedText = String.format("RMS: %.3f", rms)
        runOnUiThread {
            textView.text = formattedText
        }


    }



    /**
     * Allows the user to capture measurement data to then export to csv.
     * @param[idWidth] Our 0-1 distance measurement
     * @param[idHeight] Our 1-2 distance measurement
     */
    fun captureMeasurement(idWidth: Double, idHeight: Double) {

        //Create a layout for both entries
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL

        //Name text entry
        val textEntry = EditText(this)
        textEntry.hint = "Enter Window Name.."

        //Amount text entry
        val intEntry = EditText(this)
        intEntry.inputType = InputType.TYPE_CLASS_NUMBER
        intEntry.hint = "Enter Window Amount Number.."


        layout.addView(textEntry)
        layout.addView(intEntry)

        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder

            .setTitle("Measurement Details")
            .setMessage("The following measurement data will be saved: \n" +
                    "Width: $idWidth\n" +
                    "Height: $idHeight\n")
            .setView(layout)
            .setPositiveButton("Confirm") { dialog, which ->
                val name = textEntry.text.toString()
                val amtEntry = intEntry.text.toString()

                if (name.isEmpty() && amtEntry.isEmpty()) {
                    Toast.makeText(this,"Measurement name and amount required.", Toast.LENGTH_LONG).show()
                }
                else if (name.isEmpty()) {
                    Toast.makeText(this,"Measurement name required.", Toast.LENGTH_LONG).show()
                }
                else if (amtEntry.isEmpty()) {
                    Toast.makeText(this,"Window amount required.", Toast.LENGTH_LONG).show()
                }
                else {
                    val amount = amtEntry.toInt()
                    val windowData = WindowData(name, idWidth, idHeight, amount)
                    val measurementName = windowData.name
                    val measurementCsv : String = CsvUtils.formatCsvString(windowData)

                    saveMeasurementData(measurementCsv, measurementName)
                }


            }
            .setNegativeButton("Cancel") { dialog, which ->
                Toast.makeText(this,"Measurement cancelled, please try again!", Toast.LENGTH_LONG).show()
            }


        //Create our dialog alert
        val dialog: AlertDialog = builder.create()
        dialog.show()

    }


    /**
     * Allows saving of measurement data to a "measurements.csv" file
     *
     * @param[measurement] The measurement string formatted for csv writing.
     * @param[name] The measurement name as returned from the WindowData class.
     */
    fun saveMeasurementData(measurement : String, name : String) {
        //Link to the measurements csv file, if not created then create one and write the header
        val csvFile = CsvUtils.checkCsv("measurements", this)

        csvFile.appendText(measurement)
        runOnUiThread {
            Toast.makeText(this,"Measurement '${name}' saved!", Toast.LENGTH_LONG).show()
        }

    }
    /**
     * Converts the window measurement value from decimal format to 32nds of an inch for display.
     *
     * @param[inches] a Double holding the measured distance of two of the aruco markers, to be converted
     * @return[wholeInches] an integer value holding the amount of 32nds of an inch the window is calculated at.
     */
    fun decimalTo32nds(inches: Double): String {
        val wholeInches = inches.toInt()
        val remainder = inches - wholeInches
        val thirtySeconds = (remainder * 32).roundToInt()

        return if (thirtySeconds == 0) {
            "$wholeInches\""
        } else if (thirtySeconds == 32) {
            "${wholeInches + 1}\""
        } else {
            "$wholeInches $thirtySeconds/32\""
        }
    }






}


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

/**
 * Acts as a storage device for the window measurements and name
 *
 * @param[name] The window name entered by the user
 * @param[width] The width of the given measurement
 * @param[height] The height of the given measurement
 */
data class WindowData(
    val name : String,
    val width : Double,
    val height : Double,
    val amount : Int
)

