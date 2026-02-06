package com.apptorise.orbit.connect.core

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class BaseRepository {

    /**
     * offline-first resource:
     * - local db is the absolute source of truth and is always observed.
     * - emits local immediately if not empty.
     * - if local is empty, it stays in loading until remote fetch completes.
     * - any changes to local db at any time will trigger a new emission.
     */
    fun <Local, Remote, Domain> syncResource(
        query: () -> Flow<Local>,
        fetch: suspend () -> Remote,
        saveFetchResult: suspend (Remote) -> Unit,
        mapToDomain: (Local) -> Domain,
        shouldFetch: (Local?) -> Boolean = { true }
    ): Flow<Result<Domain>> = channelFlow {
        val networkStatus = MutableStateFlow<NetworkStatus>(NetworkStatus.Idle)

        // 1. start the fetch process immediately
        launch {
            try {
                val localPeek = query().firstOrNull()
                if (shouldFetch(localPeek)) {
                    networkStatus.value = NetworkStatus.Fetching
                    val remote = fetch()
                    saveFetchResult(remote)
                    networkStatus.value = NetworkStatus.Success
                } else {
                    networkStatus.value = NetworkStatus.Success
                }
            } catch (e: Exception) {
                networkStatus.value = NetworkStatus.Error(e)
            }
        }

        // 2. always observe the source of truth (local db)
        // we combine it with network status to decide whether to show loading or data
        query().combine(networkStatus) { localData, status ->
            val isEmpty = isDataEmpty(localData)
            val domain = mapToDomain(localData)

            when {
                // if we have data, we show it immediately. local is truth.
                !isEmpty -> Result.Success(domain)

                // if empty and we are still fetching, keep loading
                isEmpty && status is NetworkStatus.Fetching -> Result.Loading

                // if empty and network failed, show the error
                isEmpty && status is NetworkStatus.Error -> {
                    Result.Error(
                        message = status.exception.localizedMessage?.lowercase() ?: "fetch failed",
                        exception = status.exception
                    )
                }

                // if empty and network finished (or idle), show the empty success
                isEmpty && status is NetworkStatus.Success -> Result.Success(domain)

                // default starting state
                else -> Result.Loading
            }
        }
            .distinctUntilChanged()
            .collect { send(it) }
    }

    /**
     * internal network states to coordinate with local observer
     */
    private sealed interface NetworkStatus {
        data object Idle : NetworkStatus
        data object Fetching : NetworkStatus
        data object Success : NetworkStatus
        data class Error(val exception: Exception) : NetworkStatus
    }

    /**
     * perform a remote-only operation (no local storage).
     */
    fun <Remote, Domain> performRemote(
        action: suspend () -> Remote,
        mapToDomain: (Remote) -> Domain,
        onSuccess: (suspend (Domain) -> Unit)? = null
    ): Flow<Result<Domain>> = flow {
        emit(Result.Loading)
        try {
            val result = action()
            val domain = mapToDomain(result)
            onSuccess?.invoke(domain)
            emit(Result.Success(domain))
        } catch (e: Exception) {
            emit(
                Result.Error(
                    message = e.localizedMessage?.lowercase() ?: "operation failed",
                    exception = e
                )
            )
        }
    }

    /**
     * sync continuous remote stream with local db.
     */
    fun <Local, Remote, Domain> syncStream(
        query: () -> Flow<Local>,
        remoteStream: Flow<Remote>,
        saveStreamItem: suspend (Remote) -> Unit,
        mapToDomain: (Local) -> Domain
    ): Flow<Result<Domain>> = channelFlow {
        launch {
            query().collect { local ->
                send(Result.Success(mapToDomain(local)))
            }
        }

        launch {
            try {
                remoteStream.collect { remote ->
                    saveStreamItem(remote)
                }
            } catch (e: Exception) {
                send(
                    Result.Error(
                        message = e.localizedMessage?.lowercase() ?: "stream error",
                        exception = e
                    )
                )
            }
        }
    }

    /**
     * utility: check if local data is empty.
     */
    protected fun isDataEmpty(data: Any?): Boolean {
        return when (data) {
            null -> true
            is Collection<*> -> data.isEmpty()
            else -> false
        }
    }
}