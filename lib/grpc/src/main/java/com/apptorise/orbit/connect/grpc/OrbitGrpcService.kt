package com.apptorise.orbit.connect.grpc

import com.apptorise.orbit.connect.core.OrbitEngine
import com.apptorise.orbit.connect.core.Result
import io.grpc.StatusException
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.flow.Flow

open class OrbitGrpcService(
    override val isStub: Boolean
) : OrbitEngine() {

    protected fun <R> grpcCall(
        stubProvider: suspend () -> R,
        block: suspend () -> R
    ): Flow<Result<R>> = execute(
        stubCall = stubProvider,
        remoteCall = {
            try {
                block()
            } catch (e: StatusRuntimeException) {
                throw Exception(e.status.description ?: e.status.code.name)
            } catch (e: StatusException) {
                throw Exception(e.status.description ?: e.status.code.name)
            }
        }
    )
}