package com.apptorise.orbit.connect.sample.services

import com.apptorise.orbit.connect.core.Result
import com.apptorise.orbit.connect.core.network.ErrorParser
import com.apptorise.orbit.connect.http.ktor.OrbitHttpService
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val id: Int? = null,
    val title: String,
    val body: String = "Content from OrbitConnect",
    val userId: Int = 1
)

class HttpTestService(
    client: HttpClient,
    errorParser: ErrorParser,
    isStub: Boolean
) : OrbitHttpService(client, errorParser, isStub) {

    private val baseUrl = "https://jsonplaceholder.typicode.com/posts"

    fun getAll(): Flow<Result<List<Post>>> = get(
        path = baseUrl,
        stubProvider = { listOf(Post(1, "Stub Title")) }
    )

    fun create(post: Post): Flow<Result<Post>> = post(
        path = baseUrl,
        body = post,
        stubProvider = { post.copy(id = 999) }
    )

    fun update(id: Int, post: Post): Flow<Result<Post>> = put(
        path = "$baseUrl/$id",
        body = post,
        stubProvider = { post.copy(id = id) }
    )

    fun deleteOne(id: Int): Flow<Result<Unit>> = delete(
        path = "$baseUrl/$id",
        stubProvider = { Unit }
    )
}