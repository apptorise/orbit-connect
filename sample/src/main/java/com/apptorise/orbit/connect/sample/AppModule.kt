package com.apptorise.orbit.connect.sample

import android.content.Context
import com.apptorise.orbit.connect.grpc.IOrbitConnectConfig
import com.apptorise.orbit.connect.grpc.ITokenRefresher
import com.apptorise.orbit.connect.sample.services.GrpcTestService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTokenRefresher(@ApplicationContext context: Context): ITokenRefresher = GlobalTokenRefresher(context)

    @Provides
    @Singleton
    fun provideOrbitConfig(@ApplicationContext context: Context, tokenRefresher: ITokenRefresher): IOrbitConnectConfig = object : IOrbitConnectConfig {
        override val context: Context = context
        override val tokenRefresher: ITokenRefresher = tokenRefresher
        override val isStub: Boolean = true
    }

    @Provides
    @Singleton
    fun provideGrpcService(config: IOrbitConnectConfig): GrpcTestService = GrpcTestService(config)

}