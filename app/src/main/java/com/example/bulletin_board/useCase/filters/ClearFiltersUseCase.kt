package com.example.bulletin_board.useCase.filters

import jakarta.inject.Inject

class ClearFiltersUseCase
    @Inject
    constructor() {
        operator fun invoke(): MutableMap<String, String> = mutableMapOf()
    }
