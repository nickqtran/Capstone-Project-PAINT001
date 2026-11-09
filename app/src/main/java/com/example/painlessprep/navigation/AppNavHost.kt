package com.example.painlessprep.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.painlessprep.ui.JobsScreen
import com.example.painlessprep.ui.SignInScreen
import com.example.painlessprep.ui.WelcomeScreen

@Composable
fun AppNavHost(cameraContent: @Composable () -> Unit) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.WELCOME) {

        composable(Routes.WELCOME) {
            WelcomeScreen(
                onContinue = { _ -> navController.navigate(Routes.SIGN_IN) },
                onAlreadyHaveAccount = { navController.navigate(Routes.SIGN_IN) }
            )
        }

        composable(Routes.SIGN_IN) {
            val goToJobs: () -> Unit = {
                navController.navigate(Routes.JOBS) {
                    popUpTo(Routes.WELCOME) { inclusive = true }
                }
            }
            SignInScreen(
                onSignIn = goToJobs,
                onCreateAccount = goToJobs,
                onMeasureFirst = goToJobs
            )
        }

        composable(Routes.JOBS) {
            JobsScreen(onMeasureHouse = { navController.navigate(Routes.CAMERA) })
        }

        composable(Routes.CAMERA) {
            cameraContent()
        }
    }
}
