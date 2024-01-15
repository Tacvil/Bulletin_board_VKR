package com.example.bulletin_board.pix.models

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel

import kotlinx.parcelize.Parcelize
import java.util.*

/**
 * Created By Akshay Sharma on 17,June,2021
 * https://ak1.io
 */
@SuppressLint("ParcelCreator")
@Parcelize
data class Img(
    var headerDate: String = "",
    var contentUrl: Uri = Uri.EMPTY,
    var scrollerDate: String = "",
    var mediaType: Int = 1
) : Parcelable {
    @IgnoredOnParcel
    var selected = false

    @IgnoredOnParcel
    var position = 0
    override fun describeContents(): Int {
        TODO("Not yet implemented")
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        TODO("Not yet implemented")
    }

}

@SuppressLint("ParcelCreator")
@Parcelize
class Options : Parcelable {
    var ratio = Ratio.RATIO_AUTO
    var count = 1
    var spanCount = 4
    var path = "Pix/Camera"
    var isFrontFacing = false
    var mode = Mode.All
    var flash = Flash.Auto
    var preSelectedUrls = ArrayList<Uri>()
    var videoOptions : VideoOptions = VideoOptions()
    override fun describeContents(): Int {
        TODO("Not yet implemented")
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        TODO("Not yet implemented")
    }
}

@Parcelize
enum class Mode : Parcelable {
    All {
        override fun describeContents(): Int {
            TODO("Not yet implemented")
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            TODO("Not yet implemented")
        }
    }, Picture {
        override fun describeContents(): Int {
            TODO("Not yet implemented")
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            TODO("Not yet implemented")
        }
    }, Video {
        override fun describeContents(): Int {
            TODO("Not yet implemented")
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            TODO("Not yet implemented")
        }
    }
}
@SuppressLint("ParcelCreator")
@Parcelize
class VideoOptions : Parcelable {
    var videoBitrate : Int? = null
    var audioBitrate : Int? = null
    var videoFrameRate : Int? = null
    var videoDurationLimitInSeconds = 10
    override fun describeContents(): Int {
        TODO("Not yet implemented")
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        TODO("Not yet implemented")
    }
}

@Parcelize
enum class Flash : Parcelable {
    Disabled {
        override fun describeContents(): Int {
            TODO("Not yet implemented")
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            TODO("Not yet implemented")
        }
    }, On {
        override fun describeContents(): Int {
            TODO("Not yet implemented")
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            TODO("Not yet implemented")
        }
    }, Off {
        override fun describeContents(): Int {
            TODO("Not yet implemented")
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            TODO("Not yet implemented")
        }
    }, Auto {
        override fun describeContents(): Int {
            TODO("Not yet implemented")
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            TODO("Not yet implemented")
        }
    }
}

@Parcelize
enum class Ratio : Parcelable {
    RATIO_4_3 {
        override fun describeContents(): Int {
            TODO("Not yet implemented")
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            TODO("Not yet implemented")
        }
    }, RATIO_16_9 {
        override fun describeContents(): Int {
            TODO("Not yet implemented")
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            TODO("Not yet implemented")
        }
    }, RATIO_AUTO {
        override fun describeContents(): Int {
            TODO("Not yet implemented")
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            TODO("Not yet implemented")
        }
    }
}

internal class ModelList(
    var list: ArrayList<Img> = ArrayList(),
    var selection: ArrayList<Img> = ArrayList()
)