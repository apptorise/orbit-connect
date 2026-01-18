package com.apptorise.orbit.connect.sample

import com.apptorise.orbit.connect.sample.services.GrpcTestService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGrpcService(): GrpcTestService = GrpcTestService(isStub = false)

}