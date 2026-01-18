package com.apptorise.orbit.connect.sample.services

import com.apptorise.orbit.connect.core.Result
import com.apptorise.orbit.connect.grpc.OrbitGrpcService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

class GrpcTestService(
    override val isStub: Boolean
) : OrbitGrpcService(isStub) {

    fun testGrpcCall(): Flow<Result<String>> = grpcCall(
        stubProvider = { "Success from gRPC Stub" },
        block = {
            delay(1000)
            "Success from Real gRPC Server"
        }
    )
}