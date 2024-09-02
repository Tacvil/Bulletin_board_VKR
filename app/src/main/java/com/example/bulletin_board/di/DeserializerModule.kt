package com.example.bulletin_board.di

import com.example.bulletin_board.adapterFirestore.JsonDeserializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DeserializerModule {
    @Provides
    @Singleton
    fun provideJsonDeserializer(): JsonDeserializer = JsonDeserializer
}
