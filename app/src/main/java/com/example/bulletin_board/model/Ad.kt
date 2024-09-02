package com.example.bulletin_board.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
data class Ad
    @JvmOverloads
    constructor(
        @PrimaryKey
        val key: String,
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
        val favUids: List<String> = listOf(),
        var favCounter: String = "0",
        var viewsCounter: Int = 0,
        var emailCounter: String = "0",
        var callsCounter: String = "0",
    ) : Parcelable {
        fun toMap(): Map<String, Any?> =
            mapOf(
                "key" to key,
                "title" to title,
                "keyWords" to keyWords,
                "country" to country,
                "city" to city,
                "index" to index,
                "tel" to tel,
                "withSend" to withSend,
                "category" to category,
                "price" to price,
                "description" to description,
                "email" to email,
                "mainImage" to mainImage,
                "image2" to image2,
                "image3" to image3,
                "uid" to uid,
                "time" to time,
                "isPublished" to isPublished,
                "isFav" to isFav,
                "favUids" to favUids,
                "favCounter" to favCounter,
                "viewsCounter" to viewsCounter,
                "emailCounter" to emailCounter,
                "callsCounter" to callsCounter,
            )

        override fun describeContents(): Int = 0

        override fun writeToParcel(
            parcel: Parcel,
            flags: Int,
        ) {
            parcel.writeString(key)
            parcel.writeString(title)
            parcel.writeStringList(keyWords)
            parcel.writeString(country)
            parcel.writeString(city)
            parcel.writeString(index)
            parcel.writeString(tel)
            parcel.writeString(withSend)
            parcel.writeString(category)
            parcel.writeValue(price)
            parcel.writeString(description)
            parcel.writeString(email)
            parcel.writeString(mainImage)
            parcel.writeString(image2)
            parcel.writeString(image3)
            parcel.writeString(uid)
            parcel.writeString(time)
            parcel.writeByte(if (isPublished) 1 else 0)
            parcel.writeByte(if (isFav) 1 else 0)
            parcel.writeStringList(favUids)
            parcel.writeString(favCounter)
            parcel.writeInt(viewsCounter)
            parcel.writeString(emailCounter)
            parcel.writeString(callsCounter)
        }

        companion object CREATOR : Parcelable.Creator<Ad> {
            override fun createFromParcel(parcel: Parcel): Ad =
                Ad(
                    key = parcel.readString()!!,
                    title = parcel.readString(),
                    keyWords = parcel.createStringArrayList(),
                    country = parcel.readString(),
                    city = parcel.readString(),
                    index = parcel.readString(),
                    tel = parcel.readString(),
                    withSend = parcel.readString(),
                    category = parcel.readString(),
                    price = parcel.readValue(Int::class.java.classLoader) as? Int,
                    description = parcel.readString(),
                    email = parcel.readString(),
                    mainImage = parcel.readString(),
                    image2 = parcel.readString(),
                    image3 = parcel.readString(),
                    uid = parcel.readString(),
                    time = parcel.readString()!!,
                    isPublished = parcel.readByte() != 0.toByte(),
                    isFav = parcel.readByte() != 0.toByte(),
                    favUids = parcel.createStringArrayList()!!,
                    favCounter = parcel.readString()!!,
                    viewsCounter = parcel.readInt(),
                    emailCounter = parcel.readString()!!,
                    callsCounter = parcel.readString()!!,
                )

            override fun newArray(size: Int): Array<Ad?> = arrayOfNulls(size)
        }
    }
