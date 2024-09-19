package com.example.bulletin_board.di

import com.example.bulletin_board.packroom.RemoteAdDataSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object DataSourceModule {
    fun provideRemoteAdDataSource(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
    ): RemoteAdDataSource = RemoteAdDataSource(firestore, auth)
}
