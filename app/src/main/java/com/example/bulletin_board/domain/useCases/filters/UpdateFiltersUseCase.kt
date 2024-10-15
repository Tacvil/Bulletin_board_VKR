package com.example.bulletin_board.domain.useCases.filters

import jakarta.inject.Inject

class UpdateFiltersUseCase
    @Inject
    constructor() {
        operator fun invoke(
            currentFilters: MutableMap<String, String>,
            newFilters: Map<String, String>,
        ): MutableMap<String, String> = currentFilters.toMutableMap().apply { putAll(newFilters) }
    }
