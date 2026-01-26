package com.apptorise.orbit.connect.grpc

import com.apptorise.orbit.connect.core.OrbitEngine
import io.grpc.Status
import io.grpc.StatusException
import io.grpc.StatusRuntimeException

open class OrbitGrpcService(
    override val isStub: Boolean,
    private val tokenRefresher: ITokenRefresher? = null
) : OrbitEngine() {

    protected suspend fun <R> call(
        stubProvider: suspend () -> R,
        block: suspend () -> R
    ): R {
        return try {
            if (isStub) stubProvider() else block()
        } catch (e: Exception) {
            if (isUnauthenticated(e) && tokenRefresher != null) {
                if (tokenRefresher.refreshToken()) {
                    return block()
                }
            }
            throw handleException(e)
        }
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
            is StatusRuntimeException -> Exception(e.status.description ?: "gRPC Error: ${e.status.code}")
            is StatusException -> Exception(e.status.description ?: "gRPC Error: ${e.status.code}")
            else -> Exception(e.localizedMessage ?: "Unknown Transport Error")
        }
    }
}