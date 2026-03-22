package com.apptorise.orbit.connect.http.ktor

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

object HttpClientFactory {
    fun create(
        baseUrl: String,
        encodedDeviceInfo: String? = null,
        isDebug: Boolean = false,
        jsonConfig: Json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            encodeDefaults = true
        }
    ): HttpClient {
        return HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(jsonConfig)
            }

            install(DefaultRequest) {
                url(baseUrl)
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                encodedDeviceInfo?.let {
                    header("X-Device-Info", it)
                }
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 30000
                connectTimeoutMillis = 10000
                socketTimeoutMillis = 10000
            }

            if (isDebug) {
                install(Logging) {
                    level = LogLevel.ALL
                }
            }

            engine {
                config {
                    connectTimeout(10, TimeUnit.SECONDS)
                    readTimeout(30, TimeUnit.SECONDS)
                    writeTimeout(30, TimeUnit.SECONDS)
                }
            }
        }
    }
}