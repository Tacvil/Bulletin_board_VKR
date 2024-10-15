package com.example.bulletin_board.data.di

import com.example.bulletin_board.data.datasource.RemoteAdDataSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object DataSourceModule {
    fun provideRemoteAdDataSource(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        dbStorage: StorageReference,
    ): RemoteAdDataSource = RemoteAdDataSource(firestore, auth, dbStorage)
}
