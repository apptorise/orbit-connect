package com.apptorise.orbit.connect.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn

abstract class OrbitEngine {
    abstract val isStub: Boolean

    fun <R> execute(
        stubCall: suspend () -> R,
        remoteCall: suspend () -> R
    ): Flow<Result<R>> = flow {
        emit(Result.Loading)
        try {
            if (isStub) {
                delay((1000..2500L).random())
                emit(Result.Success(stubCall()))
            } else {
                emit(Result.Success(remoteCall()))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.localizedMessage ?: "Operation failed", e))
        }
    }.flowOn(Dispatchers.IO)
}