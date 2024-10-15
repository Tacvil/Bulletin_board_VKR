package com.example.bulletin_board.domain.location

interface CityDataSourceProvider {
    fun getAllCountries(): ArrayList<Pair<String, String>>

    fun getAllCities(country: String): ArrayList<Pair<String, String>>
}
