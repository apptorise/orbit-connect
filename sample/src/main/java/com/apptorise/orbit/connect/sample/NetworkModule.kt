package com.apptorise.orbit.connect.sample

import android.content.Context
import com.apptorise.orbit.connect.core.network.DefaultErrorParser
import com.apptorise.orbit.connect.core.network.ErrorParser
import com.apptorise.orbit.connect.http.ktor.HttpClientFactory
import com.apptorise.orbit.connect.http.ktor.IOrbitHttpConfig
import com.apptorise.orbit.connect.sample.services.HttpTestService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
    fun provideOrbitHttpConfig(
        @ApplicationContext context: Context,
        client: HttpClient,
        errorParser: ErrorParser
    ): IOrbitHttpConfig = object : IOrbitHttpConfig {
        override val context: Context = context
        override val client: HttpClient = client
        override val errorParser: ErrorParser = errorParser
        override val isStub: Boolean = false
        override val logTag: String = "Orbit_Nexus"
    }

    @Provides
    @Singleton
    fun provideHttpService(
        config: IOrbitHttpConfig
    ): HttpTestService = HttpTestService(config)
}