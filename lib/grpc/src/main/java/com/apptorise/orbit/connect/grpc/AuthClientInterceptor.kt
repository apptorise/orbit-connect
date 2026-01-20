package com.apptorise.orbit.connect.grpc

import io.grpc.*

class AuthClientInterceptor(private val tokenProvider: () -> String?) : ClientInterceptor {

    companion object {
        private val AUTHORIZATION_KEY = Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER)
    }

    override fun <ReqT : Any, RespT : Any> interceptCall(
        method: MethodDescriptor<ReqT, RespT>,
        callOptions: CallOptions,
        next: Channel
    ): ClientCall<ReqT, RespT> {
        return object : ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
            next.newCall(method, callOptions)
        ) {
            override fun start(responseListener: Listener<RespT>, headers: Metadata) {
                val token = tokenProvider()
                if (!token.isNullOrBlank()) {
                    headers.put(AUTHORIZATION_KEY, "Bearer $token")
                }
                super.start(responseListener, headers)
            }
        }
    }
}