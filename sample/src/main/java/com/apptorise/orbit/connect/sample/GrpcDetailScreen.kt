package com.apptorise.orbit.connect.sample

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("gRPC Unary Call") },
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
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    color = Color(0xFF03DAC6)
                )
            }

            Button(
                onClick = {
                    scope.launch {
                        service.testGrpcCall().collect { res ->
                            isLoading = res is Result.Loading
                            if (res is Result.Success) responseJson = "{ \"grpc_status\": \"SUCCESS\", \"payload\": \"${res.data}\" }"
                            if (res is Result.Error) responseJson = "{ \"grpc_status\": \"ERROR\", \"message\": \"${res.message}\" }"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF03DAC6)),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.Send, null)
                Spacer(Modifier.width(12.dp))
                Text("EXECUTE UNARY REQUEST")
            }

            Spacer(Modifier.height(24.dp))

            Box(modifier = Modifier.weight(1f)) {
                BeautifulJsonViewer(responseJson)
            }
        }
    }
}