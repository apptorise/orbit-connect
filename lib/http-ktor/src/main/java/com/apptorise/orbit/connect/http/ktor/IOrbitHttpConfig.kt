package com.apptorise.orbit.connect.http.ktor

import android.content.Context
import com.apptorise.orbit.connect.core.network.ErrorParser
import io.ktor.client.HttpClient

interface IOrbitHttpConfig {
    val context: Context
    val client: HttpClient
    val errorParser: ErrorParser
    val isStub: Boolean
    val logTag: String
}