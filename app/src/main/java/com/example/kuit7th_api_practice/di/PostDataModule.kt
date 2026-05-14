package com.example.kuit7th_api_practice.di

import com.example.kuit7th_api_practice.data.mock.InMemoryMockPostDataSource
import com.example.kuit7th_api_practice.data.mock.PostLocalDataSource
import com.example.kuit7th_api_practice.data.repository.FavoriteRepository
import com.example.kuit7th_api_practice.data.repository.PostDraftRepository
import com.example.kuit7th_api_practice.data.repositoryimpl.FavoriteRepositoryImpl
import com.example.kuit7th_api_practice.data.repositoryimpl.PostDraftRepositoryImpl
import com.example.kuit7th_api_practice.data.repositoryimpl.PostRepositoryImpl
import com.example.kuit7th_api_practice.domain.repository.PostRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PostDataModule {

    @Binds
    @Singleton
    abstract fun bindPostLocalDataSource(
        dataSource: InMemoryMockPostDataSource
    ): PostLocalDataSource

    @Binds
    @Singleton
    abstract fun bindFavoriteRepository(
        repository: FavoriteRepositoryImpl
    ): FavoriteRepository

    @Binds
    @Singleton
    abstract fun bindPostDraftRepository(
        repository: PostDraftRepositoryImpl
    ): PostDraftRepository

    @Binds
    @Singleton
    abstract fun bindPostRepository(
        repository: PostRepositoryImpl
    ): PostRepository
}
