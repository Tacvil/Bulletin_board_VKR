package com.example.bulletin_board.Room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Ad(
    @PrimaryKey
    val key: String? = null,
    val title: String? = null,
    val keyWords: List<String>? = null,
    val country: String? = null,
    val city: String? = null,
    val index: String? = null,
    val tel: String? = null,
    val withSend: String? = null,
    val category: String? = null,
    val price: Int? = null,
    val description: String? = null,
    val email: String? = null,
    var mainImage: String? = null,
    var image2: String? = null,
    var image3: String? = null,
    val uid: String? = null,
    val time: String = "0",
    val isPublished: Boolean = false,
    var isFav: Boolean = false,
    val favUids: List<String> = emptyList(),
    var favCounter: String = "0",
    var viewsCounter: Int = 0,
    var emailCounter: String = "0",
    var callsCounter: String = "0",
)
