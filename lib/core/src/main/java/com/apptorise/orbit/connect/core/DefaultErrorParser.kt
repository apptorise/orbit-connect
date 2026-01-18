package com.apptorise.orbit.connect.core.network

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

interface ErrorParser {
    fun parse(error: Any?): String
}

class DefaultErrorParser(private val json: Json) : ErrorParser {

    override fun parse(error: Any?): String {
        return when (error) {
            is String -> try {
                val element = json.parseToJsonElement(error)
                element.jsonObject["message"]?.jsonPrimitive?.content
                    ?: element.jsonObject["error"]?.jsonPrimitive?.content
                    ?: "Unknown error occurred"
            } catch (e: Exception) {
                error
            }
            is Throwable -> error.localizedMessage ?: "Connection failed"
            else -> "An unexpected error occurred"
        }
    }
}