package com.apptorise.orbit.connect.sample.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.json.*

@Composable
fun BeautifulJsonViewer(jsonString: String?) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF1E1E1E),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        if (jsonString.isNullOrBlank()) {
            Box(contentAlignment = Alignment.Center) {
                Text("No JSON data available", color = Color.Gray)
            }
        } else {
            val jsonElement = remember(jsonString) {
                try {
                    Json.parseToJsonElement(jsonString)
                } catch (e: Exception) {
                    JsonPrimitive("Invalid JSON format")
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                item {
                    JsonNodeView(key = null, element = jsonElement, depth = 0)
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
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(indent))

            if (element is JsonObject || element is JsonArray) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Spacer(modifier = Modifier.width(16.dp))
            }

            if (key != null) {
                Text(
                    text = "\"$key\": ",
                    color = Color(0xFF9CDCFE),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            JsonValueView(element, isExpanded)
        }

        if (isExpanded) {
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

@Composable
fun JsonValueView(element: JsonElement, isExpanded: Boolean) {
    when (element) {
        is JsonObject -> Text("{ ... }", color = Color.Gray, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
        is JsonArray -> Text("[ ... ]", color = Color.Gray, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
        is JsonPrimitive -> {
            val content = element.content
            val color = when {
                element.isString -> Color(0xFFCE9178)
                content == "true" || content == "false" -> Color(0xFF569CD6)
                content.toDoubleOrNull() != null -> Color(0xFFB5CEA8)
                else -> Color.White
            }
            Text(
                text = if (element.isString) "\"$content\"" else content,
                color = color,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp
            )
        }
    }
}