package com.apptorise.orbit.connect.grpc

import android.util.Log
import com.google.protobuf.MessageOrBuilder
import com.google.protobuf.util.JsonFormat
import io.grpc.*

class OrbitGrpcLogger(
    private val tag: String = "Orbit_Nexus",
    private val hostInfo: String = ""
) : ClientInterceptor {
    private val printer = JsonFormat.printer()
        .includingDefaultValueFields()
        .preservingProtoFieldNames()

    override fun <ReqT, RespT> interceptCall(
        method: MethodDescriptor<ReqT, RespT>,
        callOptions: CallOptions,
        next: Channel
    ): ClientCall<ReqT, RespT> {
        val methodName = method.fullMethodName

        return object : ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
            next.newCall(method, callOptions)
        ) {
            override fun sendMessage(message: ReqT) {
                val requestJson = if (message is MessageOrBuilder) {
                    printer.print(message)
                } else {
                    message.toString()
                }

                Log.d(tag, """
                    ┌── 🚀 REQUEST
                    │ Method: $methodName
                    │ Host:   $hostInfo
                    │ Body:   $requestJson
                    └──────────────────────────────────────────────────
                """.trimIndent())
                super.sendMessage(message)
            }

            override fun start(listener: Listener<RespT>, headers: Metadata) {
                val forwardingListener = object : ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(listener) {
                    override fun onMessage(message: RespT) {
                        val responseJson = if (message is MessageOrBuilder) {
                            printer.print(message)
                        } else {
                            message.toString()
                        }

                        Log.d(tag, """
                            ┌── ✨ RESPONSE
                            │ Method: $methodName
                            │ Data:   $responseJson
                            └──────────────────────────────────────────────────
                        """.trimIndent())
                        super.onMessage(message)
                    }

                    override fun onClose(status: Status, trailers: Metadata) {
                        val icon = if (status.isOk) "✔️" else "🆘"
                        val logMessage = """
                            $icon SESSION_END: $methodName
                            Status:  ${status.code} ${status.description ?: ""}
                            Headers: $trailers
                            __________________________________________________
                        """.trimIndent()

                        if (status.isOk) {
                            Log.d(tag, logMessage)
                        } else {
                            Log.e(tag, logMessage)
                        }
                        super.onClose(status, trailers)
                    }
                }
                super.start(forwardingListener, headers)
            }
        }
    }
}