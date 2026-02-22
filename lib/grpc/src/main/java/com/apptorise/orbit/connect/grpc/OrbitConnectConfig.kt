package com.apptorise.orbit.connect.grpc

import android.content.Context

interface IOrbitConnectConfig {
    val context: Context
    val tokenRefresher: ITokenRefresher
    val isStub: Boolean
}