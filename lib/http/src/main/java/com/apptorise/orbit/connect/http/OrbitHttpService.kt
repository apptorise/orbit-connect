package com.apptorise.orbit.connect.http

import com.apptorise.orbit.connect.core.ErrorParser
import com.apptorise.orbit.connect.core.OrbitEngine
import com.apptorise.orbit.connect.core.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow

open class OrbitHttpService(
    @PublishedApi internal val client: HttpClient,
    @PublishedApi internal val errorParser: ErrorParser,
    override val isStub: Boolean
) : OrbitEngine() {

    inline fun <reified R : Any> request(
        method: HttpMethod,
        path: String,
        body: Any? = null,
        headers: Map<String, String> = emptyMap(),
        noinline stubProvider: suspend () -> R
    ): Flow<Result<R>> = execute(
        stubCall = stubProvider,
        remoteCall = {
            val response = client.request(path) {
                this.method = method
                headers.forEach { (key, value) -> header(key, value) }
                if (method != HttpMethod.Get && body != null) {
                    setBody(body)
                }
            }

            if (response.status.isSuccess()) {
                response.body<R>()
            } else {
                val errorContent = response.bodyAsText()
                val message = errorParser.parseError(errorContent, response.status.value)
                throw Exception(message)
            }
        }
    )

    protected inline fun <reified R : Any> get(
        path: String,
        headers: Map<String, String> = emptyMap(),
        noinline stubProvider: suspend () -> R
    ): Flow<Result<R>> = request(HttpMethod.Get, path, null, headers, stubProvider)

    protected inline fun <reified R : Any> post(
        path: String,
        body: Any? = null,
        headers: Map<String, String> = emptyMap(),
        noinline stubProvider: suspend () -> R
    ): Flow<Result<R>> = request(HttpMethod.Post, path, body, headers, stubProvider)

    protected inline fun <reified R : Any> put(
        path: String,
        body: Any? = null,
        headers: Map<String, String> = emptyMap(),
        noinline stubProvider: suspend () -> R
    ): Flow<Result<R>> = request(HttpMethod.Put, path, body, headers, stubProvider)

    protected inline fun <reified R : Any> delete(
        path: String,
        headers: Map<String, String> = emptyMap(),
        noinline stubProvider: suspend () -> R
    ): Flow<Result<R>> = request(HttpMethod.Delete, path, null, headers, stubProvider)

    protected inline fun <reified R : Any> patch(
        path: String,
        body: Any? = null,
        headers: Map<String, String> = emptyMap(),
        noinline stubProvider: suspend () -> R
    ): Flow<Result<R>> = request(HttpMethod.Patch, path, body, headers, stubProvider)
}