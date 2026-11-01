package com.example.painlessprep.model

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

