package com.example.bulletin_board.utils

import android.content.Context
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.util.Locale

object CityHelper {
    fun getAllCountries(context: Context): ArrayList<Pair<String, String>> {
        var tempArray = ArrayList<Pair<String, String>>()
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
                    tempArray.add(Pair(countriesNames.getString(n), "single"))
                }
            }
        } catch (e: IOException) {
        }
        return tempArray
    }

    fun getAllCities(
        country: String,
        context: Context,
    ): ArrayList<Pair<String, String>> {
        var tempArray = ArrayList<Pair<String, String>>()
        try {
            val inputStream: InputStream = context.assets.open("countriesToCities.json")
            val size: Int = inputStream.available()
            val bytesArray = ByteArray(size)
            inputStream.read(bytesArray)
            val jsonFile = String(bytesArray)
            val jsonObject = JSONObject(jsonFile)
            val cityNames = jsonObject.getJSONArray(country)

            for (n in 0 until cityNames.length()) {
                tempArray.add(Pair(cityNames.getString(n), "single"))
            }
        } catch (e: IOException) {
        }
        return tempArray
    }

    fun filterListData(
        list: ArrayList<Pair<String, String>>,
        searchText: String?,
    ): ArrayList<Pair<String, String>> {
        val tempList = ArrayList<Pair<String, String>>()
        tempList.clear()

        if (searchText == null) {
            tempList.add(Pair("No result", "empty"))
            return tempList
        }

        for (selection: Pair<String, String> in list) {
            if (selection.first.lowercase(Locale.ROOT).startsWith(searchText.lowercase(Locale.ROOT))) {
                tempList.add(Pair(selection.first, "single"))
            }
        }
        if (tempList.size == 0) tempList.add(Pair("No result", "empty"))
        return tempList
    }
}
