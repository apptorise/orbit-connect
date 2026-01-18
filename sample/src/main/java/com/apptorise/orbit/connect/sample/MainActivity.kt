package com.apptorise.orbit.connect.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.apptorise.orbit.connect.sample.screens.HttpDetailScreen
import com.apptorise.orbit.connect.sample.services.GrpcTestService
import com.apptorise.orbit.connect.sample.services.HttpTestService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var httpService: HttpTestService
    @Inject lateinit var grpcService: GrpcTestService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "hub") {
                    composable("hub") { HubScreen(navController) }
                    composable("http") { HttpDetailScreen(navController, httpService) }
                    composable("grpc") { GrpcDetailScreen(navController, grpcService) }
                }
            }
        }
    }
}