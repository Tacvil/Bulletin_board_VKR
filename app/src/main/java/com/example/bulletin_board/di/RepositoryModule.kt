package com.example.bulletin_board.di

import com.example.bulletin_board.data.datasource.RemoteAdDataSource
import com.example.bulletin_board.data.repository.AdRepositoryImpl
import com.example.bulletin_board.domain.repository.AdRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    fun provideAdRepository(remoteAdDataSource: RemoteAdDataSource): AdRepository = AdRepositoryImpl(remoteAdDataSource)
}
