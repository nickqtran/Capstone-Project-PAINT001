package com.example.painlessprep.navigation

object Routes {
    /*TODO: Add variable names for each screen e.g. var WelcomeScreen = welcome*/
    const val WELCOME = "welcome"
    const val SIGN_IN = "sign_in"
    const val JOBS = "jobs"
    const val CAMERA = "camera/{roomId}"
    const val SIGN_UP = "sign_up"

    const val PROJECT_DETAILS = "project_details/{houseId}"
    fun projectDetails(houseId: Int): String {
        return "project_details/$houseId"
    }
    fun cameraScreen(roomId: Int): String {
        return "camera/$roomId"
    }

}