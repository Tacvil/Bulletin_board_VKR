package com.example.bulletin_board.di

import androidx.fragment.app.FragmentActivity
import com.example.bulletin_board.domain.FilterReader
import com.example.bulletin_board.domain.OrderByFilterDialog
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
object OrderByFilterDialogManagerModule {
    @Provides
    fun provideFilterReader(activity: FragmentActivity): FilterReader = activity as FilterReader

    @Provides
    fun provideOrderByFilterDialog(activity: FragmentActivity): OrderByFilterDialog = activity as OrderByFilterDialog
}
