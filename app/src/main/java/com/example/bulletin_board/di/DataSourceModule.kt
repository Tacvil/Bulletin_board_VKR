package com.example.bulletin_board.di

import com.example.bulletin_board.Room.RemoteAdDataSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
class DataSourceModule {
    @Provides
    fun provideRemoteAdDataSource(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
    ): RemoteAdDataSource = RemoteAdDataSource(firestore, auth)
}
