package com.apptorise.orbit.connect.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun HubScreen(navController: NavController) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0D0D0D)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(Modifier.height(48.dp))

            Text(
                text = "Orbit",
                style = MaterialTheme.typography.displayMedium,
                color = Color.White,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            )
            Text(
                text = "Connect Explorer",
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFF6200EE),
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Select a protocol to inspect network traffic and test real-time connectivity.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(end = 48.dp)
            )

            Spacer(Modifier.height(48.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ProtocolCard(
                    title = "HTTP Ktor Client",
                    subtitle = "RESTful API testing with CRUD operations",
                    icon = Icons.Default.Http,
                    accentColor = Color(0xFF6200EE),
                    onClick = { navController.navigate("http") }
                )

                ProtocolCard(
                    title = "gRPC Protobuf",
                    subtitle = "Unary RPC calls via Retail Admin Protos",
                    icon = Icons.Default.Terminal,
                    accentColor = Color(0xFF03DAC6),
                    onClick = { navController.navigate("grpc") }
                )
            }

            Spacer(Modifier.weight(1f))

            Text(
                text = "v1.0.0-alpha • 2026-01-18",
                style = MaterialTheme.typography.labelSmall,
                color = Color.DarkGray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun ProtocolCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFF161616), Color(0xFF121212))
                )
            )
            .border(1.dp, Color(0xFF2D2D2D), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                color = accentColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxSize()
                )
            }

            Spacer(Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.DarkGray
            )
        }
    }
}