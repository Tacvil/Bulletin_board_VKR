package com.example.bulletin_board.useCase.search

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

            for (title in results) {
                val words = title.split("\\s+".toRegex())

                when {
                    spaceCount == 0 -> formattedResults.add(Pair(words[spaceCount], "search"))
                    spaceCount > 0 -> {
                        val phraseBuilder = StringBuilder()
                        phraseBuilder.append(inputSearchQuery.substringBeforeLast(' ')).append(" ")
                        words.getOrNull(spaceCount)?.let { phraseBuilder.append(it) }
                        formattedResults.add(Pair(phraseBuilder.toString(), "search"))
                    }
                }
            }

            return formattedResults
        }
    }
