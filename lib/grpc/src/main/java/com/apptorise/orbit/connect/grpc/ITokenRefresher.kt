package com.apptorise.orbit.connect.grpc

interface ITokenRefresher {
    suspend fun refreshToken(): Boolean
}