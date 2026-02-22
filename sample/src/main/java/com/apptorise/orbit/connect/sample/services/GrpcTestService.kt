package com.apptorise.orbit.connect.sample.services

import com.apptorise.orbit.connect.grpc.IOrbitConnectConfig
import com.apptorise.orbit.connect.grpc.OrbitGrpcService
import kotlinx.coroutines.delay

class GrpcTestService(
    config: IOrbitConnectConfig
) : OrbitGrpcService(config) {

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

    suspend fun testMockJsonCall(): TestResponse = call(
        mockFilePath = "mocks/test_call.json"
    ) {
        delay(1000)
        TestResponse(message = "Real Server Data")
    }
}

data class TestResponse(val message: String)