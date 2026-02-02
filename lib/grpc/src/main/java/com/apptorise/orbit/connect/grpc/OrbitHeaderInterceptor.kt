package com.apptorise.orbit.connect.grpc

import io.grpc.*

class OrbitHeaderInterceptor(
    private val headersMap: Map<String, String> = emptyMap()
) : ClientInterceptor {

    override fun <ReqT, RespT> interceptCall(
        method: MethodDescriptor<ReqT, RespT>,
        callOptions: CallOptions,
        next: Channel
    ): ClientCall<ReqT, RespT> {
        return object : ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
            next.newCall(method, callOptions)
        ) {
            override fun start(listener: Listener<RespT>, headers: Metadata) {
                headersMap.forEach { (key, value) ->
                    val metadataKey = Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER)
                    headers.removeAll(metadataKey)
                    headers.put(metadataKey, value)
                }

                super.start(listener, headers)
            }
        }
    }
}