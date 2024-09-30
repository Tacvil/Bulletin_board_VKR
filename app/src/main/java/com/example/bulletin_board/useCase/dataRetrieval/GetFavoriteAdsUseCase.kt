package com.example.bulletin_board.useCase.dataRetrieval

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.bulletin_board.adapterFirestore.FavoriteAdsPagingSource
import com.example.bulletin_board.model.Ad
import com.example.bulletin_board.packroom.AdRepository
import com.example.bulletin_board.viewmodel.FirebaseViewModel.Companion.PAGE_SIZE
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetFavoriteAdsUseCase
    @Inject
    constructor(
        private val adRepository: AdRepository,
    ) {
        operator fun invoke(): Flow<PagingData<Ad>> =
            Pager(config = PagingConfig(pageSize = PAGE_SIZE)) {
                FavoriteAdsPagingSource(adRepository)
            }.flow
    }
