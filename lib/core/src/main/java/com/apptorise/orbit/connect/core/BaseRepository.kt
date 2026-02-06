package com.apptorise.orbit.connect.core

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class BaseRepository {

    fun <Local, Remote, Domain> syncResource(
        query: () -> Flow<Local>,
        fetch: suspend () -> Remote,
        saveFetchResult: suspend (Remote) -> Unit,
        mapToDomain: (Local) -> Domain,
        shouldFetch: (Local?) -> Boolean = { true }
    ): Flow<Result<Domain>> = channelFlow {
        // Use a local variable to track the last sent state to avoid redundant emissions
        // without killing the flow using distinctUntilChanged()
        var lastEmittedIsLoading = false

        send(Result.Loading)
        lastEmittedIsLoading = true

        val localDataFirst = query().firstOrNull()
        var networkError: Exception? = null
        var networkFinished = false

        launch {
            try {
                if (shouldFetch(localDataFirst)) {
                    val remoteData = fetch()
                    saveFetchResult(remoteData)
                }
            } catch (e: Exception) {
                networkError = e
            } finally {
                networkFinished = true
            }
        }

        query().collect { data ->
            val domainData = mapToDomain(data)
            val isEmpty = isDataEmpty(data)

            when {
                !isEmpty -> {
                    lastEmittedIsLoading = false
                    send(Result.Success(domainData))
                }

                isEmpty && networkFinished && networkError != null -> {
                    lastEmittedIsLoading = false
                    send(Result.Error(
                        message = networkError?.localizedMessage?.lowercase() ?: "sync failed",
                        exception = networkError
                    ))
                }

                isEmpty && !networkFinished -> {
                    if (!lastEmittedIsLoading) {
                        send(Result.Loading)
                        lastEmittedIsLoading = true
                    }
                }

                isEmpty && networkFinished -> {
                    lastEmittedIsLoading = false
                    send(Result.Success(domainData))
                }
            }
        }
    }

    private fun isDataEmpty(data: Any?): Boolean {
        return when (data) {
            null -> true
            is Collection<*> -> data.isEmpty()
            else -> false
        }
    }

    fun <Remote, Domain> performRemote(
        action: suspend () -> Remote,
        mapToDomain: (Remote) -> Domain,
        onSuccess: (suspend (Domain) -> Unit)? = null
    ): Flow<Result<Domain>> = flow {
        emit(Result.Loading)
        try {
            val result = action()
            val domainModel = mapToDomain(result)
            onSuccess?.invoke(domainModel)
            emit(Result.Success(domainModel))
        } catch (e: Exception) {
            emit(Result.Error(message = e.localizedMessage?.lowercase() ?: "operation failed", exception = e))
        }
    }

    fun <Local, Remote, Domain> syncStream(
        query: () -> Flow<Local>,
        remoteStream: Flow<Remote>,
        saveStreamItem: suspend (Remote) -> Unit,
        mapToDomain: (Local) -> Domain
    ): Flow<Result<Domain>> = flow {
        emit(Result.Loading)

        val networkJob = remoteStream
            .onEach { saveStreamItem(it) }
            .catch { emit(Result.Error(message = it.localizedMessage?.lowercase() ?: "stream error", exception = it)) }

        emitAll(
            merge(
                networkJob.map { Result.Loading },
                query().map { Result.Success(mapToDomain(it)) }
            )
        )
    }
}