package com.example.painlessprep.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import com.example.painlessprep.viewmodel.DatabaseViewModel

private val PrimaryColor = Color(0xFF002856)
private val BackgroundColor = Color(0xFFF0EEED)

@Composable
fun ProjectDetailsScreen(
    houseId: Int,
    databaseViewModel: DatabaseViewModel,
    onBack: () -> Unit
) {
    // update and view houses and their related rooms/windows
    val houseFlow = remember(houseId, databaseViewModel) {
        databaseViewModel.house(houseId)
    }

    val project by houseFlow.collectAsState(initial = null)

    // room creation/deletion dialogs
    var showRoomDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var roomName by remember { mutableStateOf("") }

    // store window values
    var selectedRoomId by remember { mutableStateOf<Int?>(null) }
    var windowName by remember { mutableStateOf("") }
    var windowWidth by remember { mutableStateOf("") }
    var windowHeight by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        // back button
        TextButton(
            onClick = { onBack() },
            modifier = Modifier.padding(
                start = 12.dp,
                top = 42.dp,
                bottom = 12.dp
            ),
            colors = ButtonDefaults.textButtonColors(contentColor = PrimaryColor)
        ) {
            // back arrow
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Jobs")
        }



        if (project == null) {
            Text(
                text = "Loading project...",
                modifier = Modifier.padding(20.dp)
            )
        } else {
            val currentProject = project!!

            // display selected project information
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // project name, address, and rooms
                item {
                    Text(
                        text = currentProject.house.name,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (currentProject.house.address.isNotBlank()) {
                        Text(currentProject.house.address)
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "${currentProject.rooms.size} rooms",
                        color = Color.Gray
                    )
                }

                // display rooms and corresponding windows
                items(
                    currentProject.rooms,
                    key = { it.room.id }
                ) { roomData ->
                    // each room manages its own deletion
                    var confirmRoomDelete by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // room name and deletion
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Text(
                                    text = roomData.room.roomName,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { confirmRoomDelete = true }) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Delete room ${roomData.room.roomName}",
                                        tint = Color.Red
                                    )
                                }
                            }

                            Text(
                                text = "${roomData.windows.size} windows",
                                color = Color.Gray
                            )

                            // display all windows for a room
                            roomData.windows.forEach { window ->
                                key(window.id) {
                                    var confirmWindowDelete by remember { mutableStateOf(false) }
                                    HorizontalDivider()

                                    // window name and deletion
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = window.windowName,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(onClick = { confirmWindowDelete = true }) {
                                            Icon(
                                                imageVector = Icons.Filled.Delete,
                                                contentDescription = "Delete window ${window.windowName}",
                                                tint = Color.Red
                                            )
                                        }
                                    }

                                    Text(
                                        text = "Width: ${window.width} | Height: ${window.height}",
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )

                                    if (confirmWindowDelete) {
                                        ConfirmDeleteDialog(
                                            title = "Delete Window?",
                                            message = "Delete '${window.windowName}'? This cannot be undone.",
                                            onConfirm = {
                                                databaseViewModel.deleteWindow(window)
                                                confirmWindowDelete = false
                                            },
                                            onDismiss = { confirmWindowDelete = false }
                                        )
                                    }
                                }
                            }

                            // add window dialog box
                            OutlinedButton(
                                onClick = {
                                    selectedRoomId = roomData.room.id
                                    windowName = ""
                                    windowWidth = ""
                                    windowHeight = ""
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("+ Add Window")
                            }
                        }
                    }

                    if (confirmRoomDelete) {
                        ConfirmDeleteDialog(
                            title = "Delete Room?",
                            message = "Delete '${roomData.room.roomName}' and all its windows? This cannot be undone.",
                            onConfirm = {
                                databaseViewModel.deleteRoom(roomData.room)
                                confirmRoomDelete = false
                            },
                            onDismiss = { confirmRoomDelete = false }
                        )
                    }
                }

                // create new room dialog
                item {
                    Button(
                        onClick = {
                            roomName = ""
                            showRoomDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryColor
                        )
                    ) {
                        Text("+ Add Room")
                    }
                }

                // confirmation dialog before deleting a project
                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            showDeleteDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Red
                        )
                    ) {
                        Text("Delete Project")
                    }
                }
            }
        }
    }

    // dialog for creating a new room
    if (showRoomDialog) {
        AlertDialog(
            onDismissRequest = { showRoomDialog = false },
            title = { Text("Create Room") },
            text = {
                OutlinedTextField(
                    value = roomName,
                    onValueChange = { roomName = it },
                    label = { Text("Room Name") },
                    placeholder = { Text("e.g. Living Room") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    enabled = roomName.isNotBlank(),
                    onClick = {
                        databaseViewModel.addRoom(
                            houseId,
                            roomName.trim()
                        )
                        showRoomDialog = false
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRoomDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // dialog for adding a window
    // *dimensions are entered manually for now*
    if (selectedRoomId != null) {
        AlertDialog(
            onDismissRequest = { selectedRoomId = null },
            title = { Text("Add Window") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = windowName,
                        onValueChange = { windowName = it },
                        label = { Text("Window Name") },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = windowWidth,
                        onValueChange = { windowWidth = it },
                        label = { Text("Width") },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = windowHeight,
                        onValueChange = { windowHeight = it },
                        label = { Text("Height") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                val width = windowWidth.toDoubleOrNull()
                val height = windowHeight.toDoubleOrNull()

                TextButton(
                    enabled = windowName.isNotBlank() &&
                            width != null && width > 0.0 &&
                            height != null && height > 0.0,
                    onClick = {
                        val roomId = selectedRoomId

                        if (roomId != null &&
                            width != null &&
                            height != null
                        ) {
                            databaseViewModel.addWindow(
                                roomId = roomId,
                                windowName = windowName.trim(),
                                width = width,
                                height = height
                            )

                            selectedRoomId = null
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedRoomId = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // confirmation dialog for project deletion
    if (showDeleteDialog && project != null) {
        val houseToDelete = project!!.house

        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            title = {
                Text("Delete Project?")
            },
            text = {
                Text(
                    "Are you sure you want to delete '${houseToDelete.name}'? " +
                            "This will permanently delete the project and all its rooms and windows."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        databaseViewModel.deleteHouse(houseToDelete)
                        onBack()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.Red
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

// confirmation dialog for deleting room/window
@Composable
private fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = Color.Red)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
