package com.example.painlessprep.ui



import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.painlessprep.data.RoomEntity

//Dialog box when pressing "Capture"
@Composable
fun CaptureDialogBox(
    width: Double,
    height: Double,
    onSave: (windowName: String) -> Unit,
    onDismiss: () -> Unit
){
    var windowName by remember {mutableStateOf("")}

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {Text("Save Window")},
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)){
                OutlinedTextField(
                    value = windowName,
                    onValueChange = {windowName = it},
                    label = {Text("Window Name")},
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)){
                    OutlinedTextField(
                        value = width.toString(),
                        onValueChange = {},
                        readOnly = true,
                        label = {Text("Width")},
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = height.toString(),
                        onValueChange = {},
                        readOnly = true,
                        label = {Text("Height")},
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = windowName.isNotBlank(),
                onClick = {
                    onSave(windowName.trim());
                    onDismiss()
                }
            ){Text("Save")}
        },
        dismissButton = { TextButton(onClick = onDismiss){Text("Cancel")} }
    )
}
@Preview(showBackground = true)
@Composable
private fun CaptureDialogBoxPreview(){
    MaterialTheme{
        CaptureDialogBox(
            width = 36.0,
            height = 48.0,
            onSave = { _-> },
            onDismiss = {}
        )
    }
}