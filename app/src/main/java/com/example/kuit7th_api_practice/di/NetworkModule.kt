package com.example.kuit7th_api_practice.di

import com.example.kuit7th_api_practice.data.api.PostApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton
import kotlin.jvm.java

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule{
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
    }

    @Provides
    @Singleton
    fun provideRetrofit(json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com")
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType()),
            ).build()

    @Provides
    @Singleton
    fun providePostService(retrofit: Retrofit): PostApiService =
        retrofit.create(PostApiService::class.java)
}