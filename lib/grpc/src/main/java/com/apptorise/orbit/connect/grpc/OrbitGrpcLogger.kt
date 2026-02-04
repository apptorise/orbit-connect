package com.apptorise.orbit.connect.grpc

import android.util.Base64
import android.util.Log
import com.google.protobuf.MessageOrBuilder
import com.google.protobuf.util.JsonFormat
import io.grpc.*
import java.nio.charset.StandardCharsets

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
                    │ Method:  $methodName
                    │ Host:    $hostInfo
                    │ Body:    $requestJson
                    └──────────────────────────────────────────────────
                """.trimIndent())
                super.sendMessage(message)
            }

            override fun start(listener: Listener<RespT>, headers: Metadata) {
                val headerLogs = StringBuilder()

                headers.keys().forEach { keyName ->
                    if (keyName.endsWith(Metadata.BINARY_HEADER_SUFFIX)) {
                        val key = Metadata.Key.of(keyName, Metadata.BINARY_BYTE_MARSHALLER)
                        val value = headers.get(key)
                        headerLogs.append("│ $keyName: [Binary Data, size: ${value?.size ?: 0} bytes]\n")
                    } else {
                        val key = Metadata.Key.of(keyName, Metadata.ASCII_STRING_MARSHALLER)
                        val value = headers.get(key)

                        if (value != null) {
                            val displayValue = try {
                                val decodedBytes = Base64.decode(value, Base64.NO_WRAP)
                                val decodedString = String(decodedBytes, StandardCharsets.UTF_8)
                                // Only show decoded if it looks like a structured object (JSON)
                                if (decodedString.contains("{") || decodedString.contains("[")) {
                                    "$value [Decoded: $decodedString]"
                                } else {
                                    value
                                }
                            } catch (e: Exception) {
                                value
                            }
                            headerLogs.append("│ $keyName: $displayValue\n")
                        }
                    }
                }

                Log.d(tag, """
                    ┌── 📑 HEADERS
                    │ Method:  $methodName
                    ${headerLogs.toString().trimEnd()}
                    └──────────────────────────────────────────────────
                """.trimIndent())

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
                            │ Host:   $hostInfo
                            │ Data:   $responseJson
                            └──────────────────────────────────────────────────
                        """.trimIndent())
                        super.onMessage(message)
                    }

                    override fun onClose(status: Status, trailers: Metadata) {
                        val icon = if (status.isOk) "✔️" else "🆘"
                        val logMessage = """
                            $icon SESSION_END: $methodName
                            Host:     $hostInfo
                            Status:   ${status.code} ${status.description ?: ""}
                            Trailers: $trailers
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