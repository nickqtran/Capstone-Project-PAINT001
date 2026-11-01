package com.example.painlessprep.processor

object CameraStateManager {
    @Volatile
    var isProcessingPaused: Boolean = false
}