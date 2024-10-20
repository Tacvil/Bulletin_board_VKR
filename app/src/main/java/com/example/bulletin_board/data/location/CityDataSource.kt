package com.example.bulletin_board.data.location

import android.content.Context
import com.example.bulletin_board.domain.location.CityDataSourceProvider
import com.example.bulletin_board.presentation.adapters.RcViewDialogSpinnerAdapter.Companion.SINGLE
import dagger.hilt.android.qualifiers.ActivityContext
import jakarta.inject.Inject
import org.json.JSONObject
import java.io.IOException

class CityDataSource
    @Inject
    constructor(
        @ActivityContext private val context: Context,
    ) : CityDataSourceProvider {
        override fun getAllCountries(): ArrayList<Pair<String, String>> =
            try {
                context.assets.open(COUNTRIES_TO_CITIES_FILE).use { inputStream ->
                    val jsonFile = inputStream.bufferedReader().use { it.readText() }
                    val jsonObject = JSONObject(jsonFile)
                    jsonObject
                        .names()
                        ?.let { names ->
                            (0 until names.length()).map { names.getString(it) to SINGLE }
                        }?.toList()
                        ?.let { ArrayList(it) } ?: ArrayList()
                }
            } catch (_: IOException) {
                ArrayList()
            }

        override fun getAllCities(country: String): ArrayList<Pair<String, String>> =
            try {
                context.assets.open(COUNTRIES_TO_CITIES_FILE).use { inputStream ->
                    val jsonFile = inputStream.bufferedReader().use { it.readText() }
                    val jsonObject = JSONObject(jsonFile)
                    val cityNames = jsonObject.getJSONArray(country)
                    (0 until cityNames.length())
                        .map { cityNames.getString(it) to SINGLE }
                        .toList()
                        .let { ArrayList(it) }
                }
            } catch (_: IOException) {
                ArrayList()
            }

        companion object {
            private const val COUNTRIES_TO_CITIES_FILE = "countriesToCities.json"
        }
    }
