package com.example.painlessprep.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.painlessprep.ui.ProjectDetailsScreen
import com.example.painlessprep.ui.AccountType
import com.example.painlessprep.ui.JobsScreen
import com.example.painlessprep.ui.SignInScreen
import com.example.painlessprep.ui.SignupScreen
import com.example.painlessprep.ui.UserType
import com.example.painlessprep.ui.WelcomeScreen
import com.example.painlessprep.ui.theme.PainlessPrepTheme
import com.example.painlessprep.viewmodel.DatabaseViewModel

@Composable
fun AppNavHost(dataVm: DatabaseViewModel = viewModel(), cameraContent: @Composable (onBackClicked: () -> Unit) -> Unit) {
    val navController = rememberNavController()

    // remembers what the user picked on the Welcome screen (null = not picked yet)
    var selectedType by rememberSaveable { mutableStateOf<UserType?>(null) }

    // Goes to Jobs and clears Welcome/Sign In/Sign Up so Back doesn't return to them
    val goToJobs: () -> Unit = {
        navController.navigate(Routes.JOBS) {
            popUpTo(Routes.WELCOME) { inclusive = true }
        }
    }

    NavHost(navController = navController, startDestination = Routes.WELCOME) {

        composable(Routes.WELCOME) {
            WelcomeScreen(
                onContinue = { userType ->
                    selectedType = userType
                    navController.navigate(Routes.SIGN_IN)
                },
                onAlreadyHaveAccount = { navController.navigate(Routes.SIGN_IN) }
            )
        }

        composable(Routes.SIGN_IN) {
            SignInScreen(
                onSignIn = goToJobs,
                onCreateAccount = {
                    if (selectedType == null) {
                        // They skipped the Welcome choice, so send them back to pick one
                        navController.popBackStack(Routes.WELCOME, inclusive = false)
                    } else {
                        navController.navigate(Routes.SIGN_UP)
                    }
                },
                onMeasureFirst = goToJobs
            )
        }

        // NEW: sign-up screen
        composable(Routes.SIGN_UP) {
            PainlessPrepTheme {
                SignupScreen(
                    accountType = AccountType.valueOf((selectedType ?: UserType.HOMEOWNER).name),
                    onSignupClick = { _ -> goToJobs() }  // no backend yet, so the form isn't saved
                )
            }
        }

        composable(Routes.JOBS) {
            JobsScreen(
                databaseViewModel = dataVm,
                onProjectClick = {houseId -> navController.navigate(Routes.projectDetails(houseId))
                }, onMeasureHouse = {
                    navController.navigate(Routes.CAMERA)
                }
            )
        }

        composable(
            route = Routes.PROJECT_DETAILS,
            arguments = listOf(
                navArgument("houseId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->

            val houseId = backStackEntry.arguments
                ?.getInt("houseId") ?: return@composable

            ProjectDetailsScreen(
                houseId = houseId,
                databaseViewModel = dataVm,
                // navigate back to JobsScreen
                onBack = {
                    navController.navigate(Routes.JOBS) {
                        popUpTo(Routes.JOBS) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routes.CAMERA) {
            cameraContent {
                navController.popBackStack()
            }
        }
    }
}