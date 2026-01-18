package com.apptorise.orbit.connect.sample.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.apptorise.orbit.connect.core.Result
import com.apptorise.orbit.connect.sample.components.BeautifulJsonViewer
import com.apptorise.orbit.connect.sample.services.HttpTestService
import com.apptorise.orbit.connect.sample.services.Post
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HttpDetailScreen(navController: NavController, service: HttpTestService) {
    val scope = rememberCoroutineScope()
    var responseJson by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedMethod by remember { mutableStateOf("GET") }
    var lastExecutedMethod by remember { mutableStateOf("") }
    val jsonPrinter = Json { prettyPrint = true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Orbit Inspector", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("JSONPlaceholder API", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
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
                    Text(
                        "REQUEST BUILDER",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF6200EE),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val methods = listOf("GET", "POST", "PUT", "DELETE")
                        methods.forEach { method ->
                            MethodChip(
                                label = method,
                                isSelected = selectedMethod == method,
                                onClick = { selectedMethod = method },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            lastExecutedMethod = selectedMethod
                            scope.launch {
                                when (selectedMethod) {
                                    "GET" -> service.getAll().collect { res ->
                                        isLoading = res is Result.Loading
                                        if (res is Result.Success) responseJson = jsonPrinter.encodeToString(res.data)
                                        if (res is Result.Error) responseJson = "{\"error\": \"${res.message}\"}"
                                    }
                                    "POST" -> service.create(Post(title = "Orbit Connect", body = "Real POST test")).collect { res ->
                                        isLoading = res is Result.Loading
                                        if (res is Result.Success) responseJson = jsonPrinter.encodeToString(res.data)
                                        if (res is Result.Error) responseJson = "{\"error\": \"${res.message}\"}"
                                    }
                                    "PUT" -> service.update(1, Post(title = "Updated Orbit")).collect { res ->
                                        isLoading = res is Result.Loading
                                        if (res is Result.Success) responseJson = jsonPrinter.encodeToString(res.data)
                                        if (res is Result.Error) responseJson = "{\"error\": \"${res.message}\"}"
                                    }
                                    "DELETE" -> service.deleteOne(1).collect { res ->
                                        isLoading = res is Result.Loading
                                        if (res is Result.Success) responseJson = "{ \"message\": \"Resource deleted successfully\" }"
                                        if (res is Result.Error) responseJson = "{\"error\": \"${res.message}\"}"
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6200EE),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("RUN REQUEST", fontWeight = FontWeight.Bold)
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
                Text("OUTPUT", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Spacer(Modifier.weight(1f))
                if (lastExecutedMethod.isNotEmpty()) {
                    Text(
                        text = "Last: $lastExecutedMethod",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier
                            .background(Color(0xFF1B2B1C), CircleShape)
                            .padding(horizontal = 10.dp, vertical = 2.dp)
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

@Composable
fun MethodChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(if (isSelected) Color(0xFF2D2D2D) else Color.Transparent)
    val textColor by animateColorAsState(if (isSelected) Color.White else Color.Gray)
    val borderColor by animateColorAsState(if (isSelected) Color(0xFF6200EE) else Color(0xFF333333))

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}