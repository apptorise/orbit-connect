package com.apptorise.orbit.connect.grpc

import io.grpc.ClientInterceptor
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import java.util.concurrent.TimeUnit

object GrpcChannelFactory {
    fun create(
        host: String,
        port: Int,
        useTls: Boolean = true,
        interceptors: List<ClientInterceptor> = emptyList()
    ): ManagedChannel {
        val builder = ManagedChannelBuilder.forAddress(host, port)

        if (useTls) {
            builder.useTransportSecurity()
        } else {
            builder.usePlaintext()
        }

        return builder
            .intercept(interceptors)
            .keepAliveTime(30, TimeUnit.SECONDS)
            .build()
    }
}