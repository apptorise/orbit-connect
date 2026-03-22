package com.apptorise.orbit.connect.sample.services

import com.apptorise.orbit.connect.core.Result
import com.apptorise.orbit.connect.http.ktor.IOrbitHttpConfig
import com.apptorise.orbit.connect.http.ktor.OrbitHttpService
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val id: Int? = null,
    val title: String,
    val body: String? = null,
    val userId: Int = 1
)

class HttpTestService(
    config: IOrbitHttpConfig
) : OrbitHttpService(config) {

    private val baseUrl = "https://jsonplaceholder.typicode.com/posts"

    fun getAll(): Flow<Result<List<Post>>> = get(
        path = baseUrl,
        stubProvider = { listOf(Post(1, "Stub Title")) }
    )

    fun getAllFromAssets(): Flow<Result<List<Post>>> = get(
        path = baseUrl,
        mockFilePath = "mocks/posts.json"
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