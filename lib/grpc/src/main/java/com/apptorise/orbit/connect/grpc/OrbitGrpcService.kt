package com.apptorise.orbit.connect.grpc

import com.apptorise.orbit.connect.core.OrbitEngine
import io.grpc.StatusException
import io.grpc.StatusRuntimeException

open class OrbitGrpcService(
    override val isStub: Boolean
) : OrbitEngine() {

    protected suspend fun <R> call(
        stubProvider: suspend () -> R,
        block: suspend () -> R
    ): R {
        return try {
            if (isStub) {
                stubProvider()
            } else {
                block()
            }
        } catch (e: StatusRuntimeException) {
            throw Exception(e.status.description ?: "gRPC Error: ${e.status.code}")
        } catch (e: StatusException) {
            throw Exception(e.status.description ?: "gRPC Error: ${e.status.code}")
        } catch (e: Exception) {
            throw Exception(e.localizedMessage ?: "Unknown Transport Error")
        }
    }
}