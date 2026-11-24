package com.example.painlessprep.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.painlessprep.viewmodel.MainViewModel
import org.opencv.android.JavaCameraView
import com.example.painlessprep.R
import com.example.painlessprep.model.WindowData
import com.example.painlessprep.viewmodel.DatabaseViewModel
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    viewModel: MainViewModel,
    dataVm: DatabaseViewModel,
    roomId: Int,
    onBackClicked: () -> Unit = {},
    onCalibrateClicked: () -> Unit,
    onCaptureClicked: () -> WindowData,
    onInfoClicked: () -> Unit,
    cameraViewFactory: (android.content.Context) -> JavaCameraView
) {
    val rmsValue by viewModel.rmsDisplay.observeAsState(initial = 0.0)
    var captured by remember {mutableStateOf<WindowData?>(null)}
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Camera Screen", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.6f)
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onInfoClicked,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.info_24px),
                            contentDescription = "Info",
                            tint = Color.White
                        )
                    }

                }
            )
        },
        bottomBar = {
            androidx.compose.material3.BottomAppBar(
                containerColor = Color.Black.copy(alpha = 0.6f)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "RMS: %.3f".format(rmsValue),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    HorizontalDivider()

                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick = onCalibrateClicked) {
                            Text("Calibrate")
                        }
                       Button(onClick = {
                            capturedWidth = viewModel.info.width
                            capturedHeight = viewModel.info.height
                            captured = onCaptureClicked()
                        }) {
                            Text("Capture")
                        }

                        captured?.let {
                            CaptureDialogBox(
                                width = capturedWidth,
                                height = capturedHeight,
                                onSave = { windowName ->
                                    dataVm.addWindow(roomId, windowName, capturedWidth, capturedHeight) {
                                        Toast.makeText(context, "Window added!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onDismiss = { captured = null }
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Respects bars' boundaries
        ) {
            AndroidView(
                factory = cameraViewFactory,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

}