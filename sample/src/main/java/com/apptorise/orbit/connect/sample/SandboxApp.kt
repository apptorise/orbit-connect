package com.apptorise.orbit.connect.sample

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.apptorise.orbit.connect.sample.screens.HttpDetailScreen

//@Composable
//fun SandboxApp() {
//    val navController = rememberNavController()
//
//    NavHost(navController = navController, startDestination = "hub") {
//        composable("hub") { HubScreen(navController) }
//        composable("http") { HttpDetailScreen(navController) }
//        composable("grpc") { GrpcScreen(navController) }
//    }
//}