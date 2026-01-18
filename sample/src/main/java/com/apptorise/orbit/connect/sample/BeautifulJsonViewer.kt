package com.apptorise.orbit.connect.sample.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.json.*

@Composable
fun BeautifulJsonViewer(
    jsonString: String?,
    isLoading: Boolean = false
) {
    val clipboardManager = LocalClipboardManager.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF1E1E1E),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (jsonString != null && !isLoading) {
                        IconButton(
                            onClick = { clipboardManager.setText(AnnotatedString(jsonString)) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                if (jsonString.isNullOrBlank() && !isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No JSON data available",
                            color = Color.DarkGray,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else if (jsonString != null) {
                    val jsonElement = remember(jsonString) {
                        try {
                            Json.parseToJsonElement(jsonString)
                        } catch (e: Exception) {
                            JsonPrimitive("Invalid JSON format: ${e.message}")
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        item {
                            JsonNodeView(key = null, element = jsonElement, depth = 0)
                        }
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        color = Color(0xFF9CDCFE),
                        strokeWidth = 3.dp
                    )
                }
            }
        }
    }
}

@Composable
fun JsonNodeView(key: String?, element: JsonElement, depth: Int) {
    var isExpanded by remember { mutableStateOf(true) }
    val indent = (depth * 16).dp

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = element is JsonObject || element is JsonArray) {
                    isExpanded = !isExpanded
                }
                .padding(vertical = 1.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(indent))

            if (element is JsonObject || element is JsonArray) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color(0xFF808080),
                    modifier = Modifier.size(14.dp)
                )
            } else {
                Spacer(modifier = Modifier.width(14.dp))
            }

            if (key != null) {
                Text(
                    text = "\"$key\"",
                    color = Color(0xFF9CDCFE),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
                Text(
                    text = ": ",
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            }

            JsonValueView(element, isExpanded)
        }

        AnimatedVisibility(visible = isExpanded) {
            Column {
                when (element) {
                    is JsonObject -> {
                        element.entries.forEach { (k, v) ->
                            JsonNodeView(key = k, element = v, depth = depth + 1)
                        }
                    }
                    is JsonArray -> {
                        element.forEachIndexed { index, v ->
                            JsonNodeView(key = index.toString(), element = v, depth = depth + 1)
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
fun JsonValueView(element: JsonElement, isExpanded: Boolean) {
    when (element) {
        is JsonObject -> {
            if (!isExpanded) {
                Text(
                    text = "{ ... }",
                    color = Color(0xFFD4D4D4),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        is JsonArray -> {
            if (!isExpanded) {
                Text(
                    text = "[ ... ]",
                    color = Color(0xFFD4D4D4),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        is JsonPrimitive -> {
            val content = element.content
            val color = when {
                element.isString -> Color(0xFFCE9178)
                content == "true" || content == "false" -> Color(0xFF569CD6)
                content.toDoubleOrNull() != null -> Color(0xFFB5CEA8)
                else -> Color(0xFFD4D4D4)
            }
            Text(
                text = if (element.isString) "\"$content\"" else content,
                color = color,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
        }
    }
}