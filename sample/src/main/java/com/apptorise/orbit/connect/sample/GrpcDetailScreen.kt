package com.apptorise.orbit.connect.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.apptorise.orbit.connect.core.Result
import com.apptorise.orbit.connect.sample.components.BeautifulJsonViewer
import com.apptorise.orbit.connect.sample.services.GrpcTestService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrpcDetailScreen(navController: NavController, service: GrpcTestService) {
    val scope = rememberCoroutineScope()
    var responseJson by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var hasExecuted by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Orbit gRPC Inspector", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Retail Admin Protos", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D0D0D),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF0D0D0D)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Surface(
                color = Color(0xFF161616),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2D2D2D)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "PROTOBUF DEFINITION",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF03DAC6),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )

                        Box(
                            modifier = Modifier
                                .background(Color(0xFF03DAC6).copy(alpha = 0.1f), CircleShape)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "UNARY",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF03DAC6),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "service GrpcTestService {",
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )
                    Text(
                        text = "  rpc testGrpcCall(Empty) returns (Payload);",
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color.LightGray,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    Text(
                        text = "}",
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            hasExecuted = true
                            scope.launch {
                                service.testGrpcCall().collect { res ->
                                    isLoading = res is Result.Loading
                                    when (res) {
                                        is Result.Success -> {
                                            responseJson = """
                                                {
                                                  "status": "OK",
                                                  "code": 0,
                                                  "payload": "${res.data}"
                                                }
                                            """.trimIndent()
                                        }
                                        is Result.Error -> {
                                            responseJson = """
                                                {
                                                  "status": "UNAVAILABLE",
                                                  "code": 14,
                                                  "details": "${res.message}"
                                                }
                                            """.trimIndent()
                                        }
                                        else -> Unit
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF03DAC6),
                            contentColor = Color(0xFF0D0D0D)
                        )
                    ) {
                        Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("EXECUTE RPC", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Terminal, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("gRPC CONSOLE", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Spacer(Modifier.weight(1f))
                if (hasExecuted && !isLoading) {
                    Text(
                        text = "Status: 200 OK",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF4CAF50)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                BeautifulJsonViewer(responseJson, isLoading)
            }
        }
    }
}