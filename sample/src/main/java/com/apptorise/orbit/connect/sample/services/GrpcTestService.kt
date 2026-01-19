package com.apptorise.orbit.connect.sample.services

import com.apptorise.orbit.connect.grpc.OrbitGrpcService
import kotlinx.coroutines.delay

class GrpcTestService(
    override val isStub: Boolean
) : OrbitGrpcService(isStub) {

    suspend fun testGrpcCall(): String = call(
        stubProvider = {
            delay(500)
            "Success from gRPC Stub"
        },
        block = {
            delay(1000)
            "Success from Real gRPC Server"
        }
    )
}