package com.apptorise.orbit.connect.sample.di

import com.apptorise.orbit.connect.core.network.DefaultErrorParser
import com.apptorise.orbit.connect.core.network.ErrorParser
import com.apptorise.orbit.connect.http.ktor.HttpClientFactory
import com.apptorise.orbit.connect.sample.services.HttpTestService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient = HttpClientFactory.create()

    @Provides
    @Singleton
    fun provideErrorParser(json: Json): ErrorParser = DefaultErrorParser(json)

    @Provides
    @Singleton
    fun provideHttpService(
        client: HttpClient,
        errorParser: ErrorParser
    ): HttpTestService = HttpTestService(
        client = client,
        isStub = false,
        errorParser = errorParser
    )
}