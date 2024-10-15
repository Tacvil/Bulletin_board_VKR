package com.example.bulletin_board.domain.useCases.search

import com.example.bulletin_board.data.utils.Result
import com.example.bulletin_board.domain.repository.AdRepository
import jakarta.inject.Inject

class GetSearchResultsUseCase
    @Inject
    constructor(
        private val repository: AdRepository,
    ) {
        suspend operator fun invoke(query: String): Result<List<String>> = repository.fetchSearchResults(query)
    }
