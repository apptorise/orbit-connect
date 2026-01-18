package com.apptorise.orbit.connect.sample

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun HubScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "OrbitConnect Hub",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(48.dp))

        Button(
            onClick = { navController.navigate("http") },
            modifier = Modifier.fillMaxWidth().height(80.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)),
            shape = MaterialTheme.shapes.large
        ) {
            Icon(Icons.Default.Http, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text("HTTP KTOR CRUD")
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate("grpc") },
            modifier = Modifier.fillMaxWidth().height(80.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF03DAC6)),
            shape = MaterialTheme.shapes.large
        ) {
            Icon(Icons.Default.Terminal, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text("gRPC UNARY DEMO")
        }
    }
}