package com.example.bulletin_board.useCase.filters

import jakarta.inject.Inject

class RemoveFromFilterUseCase
    @Inject
    constructor() {
        operator fun invoke(
            currentFilters: Map<String, String>,
            key: String,
        ): MutableMap<String, String> = currentFilters.toMutableMap().apply { remove(key) }
    }
