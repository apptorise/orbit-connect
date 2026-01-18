package com.apptorise.orbit.connect.sample.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
    val jsonPrinter = Json { prettyPrint = true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ktor HTTP CRUD") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp),
                    onClick = {
                        scope.launch {
                            service.getAll().collect { res ->
                                isLoading = res is Result.Loading
                                if (res is Result.Success) responseJson = jsonPrinter.encodeToString(res.data)
                            }
                        }
                    }
                ) { Text("GET", style = MaterialTheme.typography.labelSmall) }

                Button(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp),
                    onClick = {
                        scope.launch {
                            service.create(Post(title = "Orbit Connect", body = "Real POST test")).collect { res ->
                                isLoading = res is Result.Loading
                                if (res is Result.Success) responseJson = jsonPrinter.encodeToString(res.data)
                            }
                        }
                    }
                ) { Text("POST", style = MaterialTheme.typography.labelSmall) }

                Button(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp),
                    onClick = {
                        scope.launch {
                            service.update(1, Post(title = "Updated Orbit")).collect { res ->
                                isLoading = res is Result.Loading
                                if (res is Result.Success) responseJson = jsonPrinter.encodeToString(res.data)
                            }
                        }
                    }
                ) { Text("PUT", style = MaterialTheme.typography.labelSmall) }

                Button(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB00020)),
                    onClick = {
                        scope.launch {
                            service.deleteOne(1).collect { res ->
                                isLoading = res is Result.Loading
                                if (res is Result.Success) responseJson = "{ \"message\": \"Deleted Successfully\" }"
                            }
                        }
                    }
                ) { Text("DEL", style = MaterialTheme.typography.labelSmall) }
            }

            Spacer(Modifier.height(24.dp))

            Box(modifier = Modifier.weight(1f)) {
                BeautifulJsonViewer(responseJson)
            }
        }
    }
}