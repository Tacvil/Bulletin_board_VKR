package com.example.bulletin_board.data.location

import android.content.Context
import com.example.bulletin_board.domain.location.CityDataSourceProvider
import com.example.bulletin_board.presentation.adapter.RcViewDialogSpinnerAdapter.Companion.SINGLE
import dagger.hilt.android.qualifiers.ActivityContext
import jakarta.inject.Inject
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream

class CityDataSource
    @Inject
    constructor(
        @ActivityContext private val context: Context,
    ) : CityDataSourceProvider {
        override fun getAllCountries(): ArrayList<Pair<String, String>> {
            val tempArray = ArrayList<Pair<String, String>>()
            try {
                val inputStream: InputStream = context.assets.open("countriesToCities.json")
                val size: Int = inputStream.available()
                val bytesArray = ByteArray(size)
                inputStream.read(bytesArray)
                val jsonFile = String(bytesArray)
                val jsonObject = JSONObject(jsonFile)
                val countriesNames = jsonObject.names()
                if (countriesNames != null) {
                    for (n in 0 until countriesNames.length()) {
                        tempArray.add(Pair(countriesNames.getString(n), SINGLE))
                    }
                }
            } catch (_: IOException) {
            }
            return tempArray
        }

        override fun getAllCities(country: String): ArrayList<Pair<String, String>> {
            val tempArray = ArrayList<Pair<String, String>>()
            try {
                val inputStream: InputStream = context.assets.open("countriesToCities.json")
                val size: Int = inputStream.available()
                val bytesArray = ByteArray(size)
                inputStream.read(bytesArray)
                val jsonFile = String(bytesArray)
                val jsonObject = JSONObject(jsonFile)
                val cityNames = jsonObject.getJSONArray(country)

                for (n in 0 until cityNames.length()) {
                    tempArray.add(Pair(cityNames.getString(n), SINGLE))
                }
            } catch (_: IOException) {
            }
            return tempArray
        }
    }
