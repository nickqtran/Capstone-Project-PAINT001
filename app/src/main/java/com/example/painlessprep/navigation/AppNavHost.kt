package com.example.painlessprep.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavHost(){
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "start screen"){ /*TODO: Change "start screen" to something else like sing up/in screen*/
        /*TODO: ADD NAVIGATION*/
    }
}
