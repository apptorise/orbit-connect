package com.apptorise.orbit.connect.grpc

import io.grpc.*
import android.util.Base64

class OrbitHeaderInterceptor(
    private val userAgent: String,
    private val deviceInfoJson: String? = null
) : ClientInterceptor {

    companion object {
        private val USER_AGENT_KEY: Metadata.Key<String> = Metadata.Key.of("user-agent", Metadata.ASCII_STRING_MARSHALLER)

        private val DEVICE_INFO_KEY: Metadata.Key<String> = Metadata.Key.of("x-device-info", Metadata.ASCII_STRING_MARSHALLER)
    }

    override fun <ReqT, RespT> interceptCall(
        method: MethodDescriptor<ReqT, RespT>,
        callOptions: CallOptions,
        next: Channel
    ): ClientCall<ReqT, RespT> {
        return object : ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
            next.newCall(method, callOptions)
        ) {
            override fun start(listener: Listener<RespT>, headers: Metadata) {
                headers.put(USER_AGENT_KEY, userAgent)

                deviceInfoJson?.let { json ->
                    val encodedInfo = Base64.encodeToString(json.toByteArray(), Base64.NO_WRAP)
                    headers.put(DEVICE_INFO_KEY, encodedInfo)
                }

                super.start(listener, headers)
            }
        }
    }
}