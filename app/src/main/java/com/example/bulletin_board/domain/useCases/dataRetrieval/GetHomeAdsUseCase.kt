package com.example.bulletin_board.domain.useCases.dataRetrieval

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.bulletin_board.data.paging.AdsPagingSource
import com.example.bulletin_board.domain.model.Ad
import com.example.bulletin_board.domain.repository.AdRepository
import com.example.bulletin_board.presentation.viewModel.MainViewModel.Companion.PAGE_SIZE
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetHomeAdsUseCase
    @Inject
    constructor(
        private val adRepository: AdRepository,
    ) {
        operator fun invoke(filters: MutableMap<String, String>): Flow<PagingData<Ad>> =
            Pager(config = PagingConfig(pageSize = PAGE_SIZE)) {
                AdsPagingSource(adRepository, filters)
            }.flow
    }
