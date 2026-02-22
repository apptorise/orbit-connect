package com.apptorise.orbit.connect.grpc

import com.apptorise.orbit.connect.core.OrbitEngine
import com.google.protobuf.Message
import com.google.protobuf.util.JsonFormat
import io.grpc.Status
import io.grpc.StatusException
import io.grpc.StatusRuntimeException
import java.io.InputStreamReader

abstract class OrbitGrpcService(
    @PublishedApi internal val config: IOrbitConnectConfig
) : OrbitEngine() {

    override val isStub: Boolean
        get() = config.isStub

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
        if (isStub) {
            val inputStream = try {
                config.context.assets.open(mockFilePath)
            } catch (e: java.io.FileNotFoundException) {
                throw IllegalStateException("CRITICAL: Mock file missing at assets/$mockFilePath. Stubs cannot function without this file.")
            }

            return inputStream.use { stream ->
                val builder = (R::class.java.getMethod("newBuilder").invoke(null) as Message.Builder)
                JsonFormat.parser().ignoringUnknownFields().merge(InputStreamReader(stream), builder)
                builder.build() as R
            }
        }

        return try {
            block()
        } catch (e: Exception) {
            handleGrpcError(e) { block() }
        }
    }

    @PublishedApi
    internal suspend fun <R> handleGrpcError(
        e: Exception,
        block: suspend () -> R
    ): R {
        val isAuthError = isUnauthenticated(e)
        if (isAuthError) {
            if (config.tokenRefresher.refreshToken()) {
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