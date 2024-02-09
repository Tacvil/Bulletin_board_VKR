package com.example.bulletin_board.utils

import android.util.Log
import com.example.bulletin_board.model.AdFilter
import com.example.bulletin_board.model.Announcement

object FilterManager {
    fun createFilter(ad:Announcement): AdFilter{
        return AdFilter(
            ad.time,
            "${ad.category}_${ad.time}",
            "${ad.category}_${ad.country}_${ad.withSend}_${ad.time}",
            "${ad.category}_${ad.country}_${ad.city}_${ad.withSend}_${ad.time}",
            "${ad.category}_${ad.country}_${ad.city}_${ad.index}_${ad.withSend}_${ad.time}",
            "${ad.category}_${ad.index}_${ad.withSend}_${ad.time}",
            "${ad.category}_${ad.withSend}_${ad.time}",

            "${ad.title}_${ad.country}_${ad.withSend}_${ad.time}",
            "${ad.title}_${ad.time}",
            "${ad.title}_${ad.withSend}_${ad.time}",
            "${ad.country}_${ad.withSend}_${ad.time}",
            "${ad.country}_${ad.city}_${ad.withSend}_${ad.time}",
            "${ad.country}_${ad.city}_${ad.index}_${ad.withSend}_${ad.time}",
            "${ad.index}_${ad.withSend}_${ad.time}",
            "${ad.withSend}_${ad.time}"
        )
    }

    fun getFilter(filter: String): String{
        val sBuilderNode = StringBuilder()
        val sBuilderFilter = StringBuilder()
        val tempArray = filter.split("_")
        Log.d("FilterManager", "tempArray: $tempArray")
        if (tempArray[0] != "empty"){
            sBuilderNode.append("title_")
            sBuilderFilter.append("${tempArray[0]}")
        }
        if (tempArray[1] != "empty"){
            sBuilderNode.append("country_")
            sBuilderFilter.append("${tempArray[1]}_")
        }
        if (tempArray[2] != "empty"){
            sBuilderNode.append("city_")
            sBuilderFilter.append("${tempArray[2]}_")
        }
        if (tempArray[3] != "empty"){
            sBuilderNode.append("index_")
            sBuilderFilter.append("${tempArray[3]}_")
        }
//        sBuilderFilter.append(tempArray[4])
        sBuilderNode.append("time")

        return "$sBuilderNode|$sBuilderFilter"
    }
}