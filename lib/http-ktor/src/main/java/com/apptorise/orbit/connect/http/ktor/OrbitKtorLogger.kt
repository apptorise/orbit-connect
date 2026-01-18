package com.apptorise.orbit.connect.http.ktor

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse
import io.ktor.http.content.TextContent

class OrbitKtorLogger(
    private val tag: String = "Orbit_Nexus"
) {
    val logger = object : Logger {
        override fun log(message: String) {
            Log.d(tag, message)
        }
    }

    fun install(config: HttpClient) {
    }
}

fun HttpRequestBuilder.logRequest(tag: String) {
    val body = this.body
    val bodyString = if (body is TextContent) body.text else body.toString()
    Log.d(tag, """
        ┌── 🚀 HTTP REQUEST
        │ Method: ${this.method.value}
        │ URL:    ${this.url.buildString()}
        │ Body:   $bodyString
        └──────────────────────────────────────────────────
    """.trimIndent())
}

suspend fun HttpResponse.logResponse(tag: String, bodyString: String) {
    val icon = if (this.status.value in 200..299) "✨" else "🆘"
    Log.d(tag, """
        ┌── $icon HTTP RESPONSE
        │ Status: ${this.status.value} ${this.status.description}
        │ URL:    ${this.call.request.url}
        │ Data:   $bodyString
        └──────────────────────────────────────────────────
    """.trimIndent())
}