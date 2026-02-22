package com.apptorise.orbit.connect.sample

import android.content.Context
import com.apptorise.orbit.connect.grpc.ITokenRefresher

class GlobalTokenRefresher(private val context: Context) : ITokenRefresher {
    override suspend fun refreshToken(): Boolean {
        return true
    }
}