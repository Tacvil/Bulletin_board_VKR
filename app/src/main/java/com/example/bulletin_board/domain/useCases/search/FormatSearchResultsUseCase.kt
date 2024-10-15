package com.example.bulletin_board.domain.useCases.search

import com.example.bulletin_board.presentation.adapter.RcViewSearchSpinnerAdapter.Companion.SEARCH
import jakarta.inject.Inject

class FormatSearchResultsUseCase
    @Inject
    constructor() {
        operator fun invoke(
            results: List<String>,
            inputSearchQuery: String,
        ): List<Pair<String, String>> {
            val formattedResults = mutableListOf<Pair<String, String>>()
            val spaceCount = inputSearchQuery.count { it == ' ' }

            results.forEach { title ->
                val words = title.split("\\s+".toRegex())
                when {
                    spaceCount == 0 -> formattedResults.add(Pair(words[spaceCount], SEARCH))
                    spaceCount > 0 -> {
                        val phraseBuilder = StringBuilder().append(inputSearchQuery.substringBeforeLast(' ')).append(" ")
                        words.getOrNull(spaceCount)?.let { phraseBuilder.append(it) }
                        formattedResults.add(Pair(phraseBuilder.toString(), SEARCH))
                    }
                }
            }

            return formattedResults
        }
    }
