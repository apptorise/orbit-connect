package com.apptorise.orbit.connect.core

import kotlinx.coroutines.flow.*

abstract class BaseRepository {

    fun <Local, Remote, Domain> syncResource(
        query: () -> Flow<Local>,
        fetch: suspend () -> Remote,
        saveFetchResult: suspend (Remote) -> Unit,
        mapToDomain: (Local) -> Domain,
        shouldFetch: (Local?) -> Boolean = { it == null }
    ): Flow<Result<Domain>> = flow {
        emit(Result.Loading)

        val localData = query().firstOrNull()

        if (shouldFetch(localData)) {
            try {
                val remoteData = fetch()
                saveFetchResult(remoteData)
            } catch (e: Exception) {
                emit(Result.Error(message = e.localizedMessage ?: "Sync failed", exception = e))
            }
        }

        emitAll(query().map { Result.Success(mapToDomain(it)) })
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
            emit(Result.Error(message = e.localizedMessage ?: "Operation failed", exception = e))
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
            .catch { emit(Result.Error(message = it.localizedMessage ?: "Stream error", exception = it)) }

        emitAll(
            merge(
                networkJob.map { Result.Loading },
                query().map { Result.Success(mapToDomain(it)) }
            )
        )
    }
}