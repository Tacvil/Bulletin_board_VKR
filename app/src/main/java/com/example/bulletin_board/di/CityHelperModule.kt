package com.example.bulletin_board.di

import com.example.bulletin_board.data.location.CityDataSource
import com.example.bulletin_board.domain.location.CityDataSourceProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
abstract class CityHelperModule {
    @Binds
    abstract fun bindCityProvider(cityHelper: CityDataSource): CityDataSourceProvider
}
