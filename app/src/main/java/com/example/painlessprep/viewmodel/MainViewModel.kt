package com.example.painlessprep.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.painlessprep.model.CalibrationData
import com.example.painlessprep.model.WindowData
import com.example.painlessprep.data.CalibrationRepository
import com.example.painlessprep.data.MeasurementRepository
import com.example.painlessprep.processor.ProcessorInfo
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint2f
import org.opencv.core.MatOfPoint3f

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val calibrationRepo = CalibrationRepository(application)
    private val measurementRepo = MeasurementRepository(application)

    var isCalibrating = false
    var isProcessingCalibration = false

    var info: ProcessorInfo = object : ProcessorInfo {
        override var currentWidth: String = ""
        override var currentHeight: String = ""
        override var squareConfidence: Double = 0.0
        override var markerConfidence: Map<Int, Double> = emptyMap()
    }

    val collectedImagePoints = mutableListOf<MatOfPoint2f>()
    val collectedObjectPoints = mutableListOf<MatOfPoint3f>()

    var currentWidth: Double = 0.0
    var currentHeight: Double = 0.0

    // Private set prevents setting this var outside of this class.
    var savedCalibration: CalibrationData? = calibrationRepo.loadCalibration()
        private set

    private val _rmsDisplay = MutableLiveData<Double>()
    val rmsDisplay: LiveData<Double> = _rmsDisplay

    init {
        savedCalibration?.let { _rmsDisplay.postValue(it.rms) }
    }

    fun saveNewCalibration(cameraMatrix: Mat, distortionCoeffs: Mat, rms: Double) {
        calibrationRepo.saveCalibration(cameraMatrix, distortionCoeffs, rms)
        savedCalibration = CalibrationData(cameraMatrix, distortionCoeffs, rms)
        _rmsDisplay.postValue(rms)
    }

    fun saveMeasurement(windowData: WindowData) {
        measurementRepo.saveMeasurement(windowData)
    }
}
