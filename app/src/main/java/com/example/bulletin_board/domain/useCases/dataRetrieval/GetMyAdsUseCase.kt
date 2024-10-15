package com.example.bulletin_board.domain.useCases.dataRetrieval

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.bulletin_board.data.paging.MyAdsPagingSource
import com.example.bulletin_board.domain.model.Ad
import com.example.bulletin_board.domain.repository.AdRepository
import com.example.bulletin_board.presentation.viewModel.MainViewModel.Companion.PAGE_SIZE
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetMyAdsUseCase
    @Inject
    constructor(
        private val adRepository: AdRepository,
    ) {
        operator fun invoke(): Flow<PagingData<Ad>> =
            Pager(config = PagingConfig(pageSize = PAGE_SIZE)) {
                MyAdsPagingSource(adRepository)
            }.flow
    }
