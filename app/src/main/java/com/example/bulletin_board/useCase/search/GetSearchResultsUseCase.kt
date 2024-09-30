package com.example.bulletin_board.useCase.search

import com.example.bulletin_board.packroom.AdRepository
import com.example.bulletin_board.packroom.Result
import jakarta.inject.Inject

class GetSearchResultsUseCase
    @Inject
    constructor(
        private val repository: AdRepository,
    ) {
        suspend operator fun invoke(query: String): Result<List<String>> = repository.fetchSearchResults(query)
    }
