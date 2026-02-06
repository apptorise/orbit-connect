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
        var networkFinished = false
        var networkError: Exception? = null

        // 1. Start the network fetch in a separate coroutine
        val networkJob = launch {
            try {
                // Peek at local data to decide if we fetch
                val currentLocal = query().firstOrNull()
                if (shouldFetch(currentLocal)) {
                    val remoteData = fetch()
                    saveFetchResult(remoteData)
                }
            } catch (e: Exception) {
                networkError = e
            } finally {
                networkFinished = true
            }
        }

        // 2. Immediately collect from the database
        query().collect { data ->
            val domainData = mapToDomain(data)
            val isEmpty = isDataEmpty(data)

            when {
                // If we have any data (cached or new), show it immediately
                !isEmpty -> {
                    send(Result.Success(domainData))
                }

                // If empty and network is still working, show loading
                isEmpty && !networkFinished -> {
                    send(Result.Loading)
                }

                // If empty and network failed, show error
                isEmpty && networkFinished && networkError != null -> {
                    send(Result.Error(
                        message = networkError?.localizedMessage?.lowercase() ?: "sync failed",
                        exception = networkError
                    ))
                }

                // If empty and network finished with no data, show empty success
                isEmpty && networkFinished -> {
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