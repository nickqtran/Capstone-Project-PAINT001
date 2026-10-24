package com.example.painlessprep.ui

import android.view.SurfaceView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.painlessprep.viewmodel.MainViewModel
import org.opencv.android.JavaCameraView
import com.example.painlessprep.R

@Composable
fun CameraScreen(
    viewModel: MainViewModel,
    onCalibrateClicked: () -> Unit,
    onCaptureClicked: () -> Unit,
    onInfoClicked: () -> Unit,
    cameraViewFactory: (android.content.Context) -> JavaCameraView
) {
    val rmsValue by viewModel.rmsDisplay.observeAsState(initial = 0.0)

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Host OpenCV Camera View inside Jetpack Compose using AndroidView
        AndroidView(
            factory = cameraViewFactory,
            modifier = Modifier.fillMaxSize()
        )

        // 2. RMS Display Overlay
        Text(
            text = "RMS: %.3f".format(rmsValue),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        )

        // 3. Info Button
        IconButton(
            onClick = onInfoClicked,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(painter = painterResource(id = R.drawable.info_24px), contentDescription = "Info")
        }

        // 4. Bottom Control Buttons
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(onClick = onCalibrateClicked) {
                Text("Calibrate")
            }
            Button(onClick = onCaptureClicked) {
                Text("Capture")
            }
        }
    }
}