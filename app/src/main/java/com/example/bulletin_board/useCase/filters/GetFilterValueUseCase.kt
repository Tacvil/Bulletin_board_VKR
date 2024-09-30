package com.example.bulletin_board.useCase.filters

import jakarta.inject.Inject

class GetFilterValueUseCase
    @Inject
    constructor() {
        operator fun invoke(
            currentFilters: Map<String, String>,
            key: String,
        ): String? = currentFilters[key]
    }
