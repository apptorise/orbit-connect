package com.apptorise.orbit.connect.grpc

import android.content.Context
import com.apptorise.orbit.connect.core.OrbitEngine
import com.google.gson.Gson
import io.grpc.Status
import io.grpc.StatusException
import io.grpc.StatusRuntimeException
import java.io.InputStreamReader

open class OrbitGrpcService(
    override val isStub: Boolean,
    @PublishedApi internal val context: Context? = null,
    private val tokenRefresher: ITokenRefresher? = null
) : OrbitEngine() {

    @PublishedApi internal val gson = Gson()

    protected suspend fun <R> call(
        stubProvider: suspend () -> R,
        block: suspend () -> R
    ): R {
        return try {
            if (isStub) stubProvider() else block()
        } catch (e: Exception) {
            handleGrpcError(e, block)
        }
    }

    protected suspend inline fun <reified R> call(
        mockFilePath: String,
        crossinline block: suspend () -> R
    ): R {
        return try {
            if (isStub) {
                if (context == null) throw IllegalStateException("Context is required for mocking")

                val inputStream = try {
                    context.assets.open(mockFilePath)
                } catch (e: java.io.FileNotFoundException) {
                    throw IllegalArgumentException("Mock file not found at assets/$mockFilePath. Ensure the file exists and the path is correct.", e)
                }

                inputStream.use { stream ->
                    val reader = InputStreamReader(stream)
                    gson.fromJson(reader, R::class.java)
                }
            } else {
                block()
            }
        } catch (e: Exception) {
            if (e is IllegalArgumentException) throw e
            handleGrpcError(e) { block() }
        }
    }

    @PublishedApi
    internal suspend fun <R> handleGrpcError(
        e: Exception,
        block: suspend () -> R
    ): R {
        val isAuthError = isUnauthenticated(e)
        if (isAuthError && tokenRefresher != null) {
            if (tokenRefresher.refreshToken()) {
                return block()
            }
        }
        throw handleException(e)
    }

    private fun isUnauthenticated(e: Exception): Boolean {
        var cause: Throwable? = e
        while (cause != null) {
            val status = when (cause) {
                is StatusRuntimeException -> cause.status
                is StatusException -> cause.status
                else -> null
            }
            if (status?.code == Status.Code.UNAUTHENTICATED) return true
            cause = cause.cause
        }
        return false
    }

    private fun handleException(e: Exception): Exception {
        return when (e) {
            is StatusRuntimeException -> Exception(e.status.description ?: "gRPC Code: ${e.status.code}")
            is StatusException -> Exception(e.status.description ?: "gRPC Code: ${e.status.code}")
            else -> Exception(e.localizedMessage ?: "Unknown Transport Error")
        }
    }
}