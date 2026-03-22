package com.apptorise.orbit.connect.http.ktor

import com.apptorise.orbit.connect.core.OrbitEngine
import com.apptorise.orbit.connect.core.Result
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.prepareRequest
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import java.io.InputStreamReader

abstract class OrbitHttpService(
    @PublishedApi internal val config: IOrbitHttpConfig
) : OrbitEngine() {

    override val isStub: Boolean
        get() = config.isStub

    @PublishedApi
    internal val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    inline fun <reified R : Any> request(
        method: HttpMethod,
        path: String,
        body: Any? = null,
        headers: Map<String, String> = emptyMap(),
        noinline stubProvider: suspend () -> R
    ): Flow<Result<R>> = execute(
        stubCall = stubProvider,
        remoteCall = { performRemoteCall(method, path, body, headers) }
    )

    inline fun <reified R : Any> request(
        method: HttpMethod,
        path: String,
        mockFilePath: String,
        body: Any? = null,
        headers: Map<String, String> = emptyMap()
    ): Flow<Result<R>> = execute(
        stubCall = { loadMockData(mockFilePath) },
        remoteCall = { performRemoteCall(method, path, body, headers) }
    )

    @PublishedApi
    internal suspend inline fun <reified R : Any> performRemoteCall(
        method: HttpMethod,
        path: String,
        body: Any? = null,
        headers: Map<String, String> = emptyMap()
    ): R {
        val response = config.client.prepareRequest(path) {
            this.method = method
            headers.forEach { (key, value) -> header(key, value) }
            if (method != HttpMethod.Get && body != null) {
                setBody(body)
            }
            logRequest(config.logTag)
        }.execute()
        val responseBodyText = response.bodyAsText()
        response.logResponse(config.logTag, responseBodyText)
        if (response.status.isSuccess()) {
            return response.body<R>()
        } else {
            val message = config.errorParser.parse(responseBodyText)
            throw Exception(message)
        }
    }

    @PublishedApi
    internal inline fun <reified R : Any> loadMockData(mockFilePath: String): R {
        val inputStream = try {
            config.context.assets.open(mockFilePath)
        } catch (e: java.io.FileNotFoundException) {
            throw IllegalStateException("CRITICAL: Mock file missing at assets/$mockFilePath")
        }
        return inputStream.use { stream ->
            val content = InputStreamReader(stream).readText()
            json.decodeFromString<R>(content)
        }
    }

    protected inline fun <reified R : Any> get(
        path: String,
        noinline stubProvider: suspend () -> R
    ) = request<R>(HttpMethod.Get, path, null, emptyMap(), stubProvider)

    protected inline fun <reified R : Any> get(
        path: String,
        mockFilePath: String,
        headers: Map<String, String> = emptyMap()
    ) = request<R>(HttpMethod.Get, path, mockFilePath, null, headers)

    protected inline fun <reified R : Any> post(
        path: String,
        body: Any? = null,
        noinline stubProvider: suspend () -> R
    ) = request<R>(HttpMethod.Post, path, body, emptyMap(), stubProvider)

    protected inline fun <reified R : Any> post(
        path: String,
        mockFilePath: String,
        body: Any? = null,
        headers: Map<String, String> = emptyMap()
    ) = request<R>(HttpMethod.Post, path, mockFilePath, body, headers)

    protected inline fun <reified R : Any> put(
        path: String,
        body: Any? = null,
        noinline stubProvider: suspend () -> R
    ) = request<R>(HttpMethod.Put, path, body, emptyMap(), stubProvider)

    protected inline fun <reified R : Any> put(
        path: String,
        mockFilePath: String,
        body: Any? = null,
        headers: Map<String, String> = emptyMap()
    ) = request<R>(HttpMethod.Put, path, mockFilePath, body, headers)

    protected inline fun <reified R : Any> delete(
        path: String,
        noinline stubProvider: suspend () -> R
    ) = request<R>(HttpMethod.Delete, path, null, emptyMap(), stubProvider)

    protected inline fun <reified R : Any> delete(
        path: String,
        mockFilePath: String,
        headers: Map<String, String> = emptyMap()
    ) = request<R>(HttpMethod.Delete, path, mockFilePath, null, headers)

    protected inline fun <reified R : Any> patch(
        path: String,
        body: Any? = null,
        noinline stubProvider: suspend () -> R
    ) = request<R>(HttpMethod.Patch, path, body, emptyMap(), stubProvider)

    protected inline fun <reified R : Any> patch(
        path: String,
        mockFilePath: String,
        body: Any? = null,
        headers: Map<String, String> = emptyMap()
    ) = request<R>(HttpMethod.Patch, path, mockFilePath, body, headers)
}