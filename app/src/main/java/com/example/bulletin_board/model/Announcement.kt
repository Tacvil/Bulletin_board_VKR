package com.example.bulletin_board.model

import java.io.Serializable

data class Announcement(
    val title: String? = null,
    val country: String? = null,
    val city: String? = null,
    val index: String? = null,
    val tel: String? = null,
    val withSend: String? = null,
    val category: String? = null,
    val price: String? = null,
    val description: String? = null,
    var mainImage: String? = null,
    var image2: String? = null,
    var image3: String? = null,
    val key: String? = null,
    val uid: String? = null,
    var isFav: Boolean = false,

    var favCounter: String = "0",
    var viewsCounter: String = "0",
    var emailCounter: String = "0",
    var callsCounter: String = "0"
): Serializable
