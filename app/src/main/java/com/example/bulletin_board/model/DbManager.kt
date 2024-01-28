package com.example.bulletin_board.model

import com.example.bulletin_board.utils.FilterManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class DbManager {
    val database = Firebase.database.getReference(MAIN_NODE)
    val dbStorage = Firebase.storage.getReference(MAIN_NODE)
    val auth = Firebase.auth

    fun publishAnnouncement(announcement: Announcement, finishListener: FinishWorkListener) {
        if (auth.uid != null) database.child(announcement.key ?: "empty").child(auth.uid!!)
            .child(AD_NODE)
            .setValue(announcement).addOnCompleteListener {
                val adFilter = FilterManager.createFilter(announcement)
                database.child(announcement.key ?: "empty")
                    .child(FILTER_NODE)
                    .setValue(adFilter).addOnCompleteListener {
                        finishListener.onFinish()
                    }
            }
    }

    fun adViewed(ad: Announcement) {
        var counter = ad.viewsCounter.toInt()
        counter++
        if (auth.uid != null) database.child(ad.key ?: "empty")
            .child(INFO_NODE)
            .setValue(InfoItem(counter.toString(), ad.emailCounter, ad.callsCounter))
    }

    fun onFavClick(ad: Announcement, listener: FinishWorkListener) {
        if (ad.isFav) removeFromFavs(ad, listener) else addToFavs(ad, listener)
    }

    private fun addToFavs(ad: Announcement, listener: FinishWorkListener) {
        ad.key?.let {
            auth.uid?.let { uid ->
                database.child(it).child(FAVS_NODE)
                    .child(uid).setValue(uid).addOnCompleteListener {
                        if (it.isSuccessful) listener.onFinish()
                    }
            }
        }
    }

    private fun removeFromFavs(ad: Announcement, listener: FinishWorkListener) {
        ad.key?.let {
            auth.uid?.let { uid ->
                database.child(it).child(FAVS_NODE)
                    .child(uid).removeValue().addOnCompleteListener {
                        if (it.isSuccessful) listener.onFinish()
                    }
            }
        }
    }

    fun getMyAnnouncement(readDataCallback: ReadDataCallback?) {
        val query = database.orderByChild(auth.uid + "/announcement/uid").equalTo(auth.uid)
        readDataFromDb(query, readDataCallback)
    }

    fun getMyFavs(readDataCallback: ReadDataCallback?) {
        val query = database.orderByChild("/favs/${auth.uid}").equalTo(auth.uid)
        readDataFromDb(query, readDataCallback)
    }

    fun getAllAnnouncementFirstPage(filter: String, readDataCallback: ReadDataCallback?) {
        val query = if (filter.isEmpty()) {
            database.orderByChild("/adFilter/time").limitToLast(
                ADS_LIMIT
            )
        } else {
            getAllAnnouncementByFilterFirstPage(filter)
        }
        readDataFromDb(query, readDataCallback)
    }

    private fun getAllAnnouncementByFilterFirstPage(filter: String): Query {
        val orderBy = filter.split("|")[0]
        val filterAds = filter.split("|")[1]
        return database.orderByChild("/adFilter/$orderBy").startAt(filterAds)
            .endAt(filterAds + "\uf8ff").limitToLast(
            ADS_LIMIT
        )
    }

    fun getAllAnnouncementNextPage(
        time: String,
        filter: String,
        readDataCallback: ReadDataCallback?
    ) {
        if (filter.isEmpty()) {
            val query = database.orderByChild("/adFilter/time").endBefore(time).limitToLast(
                ADS_LIMIT
            )
            readDataFromDb(query, readDataCallback)
        } else {
            getAllAnnouncementByFilterNextPage(filter, time, readDataCallback)
        }
    }

    private fun getAllAnnouncementByFilterNextPage(
        filter: String,
        time: String,
        readDataCallback: ReadDataCallback?
    ) {
        val orderBy = filter.split("|")[0]
        val filterAds = filter.split("|")[1]
        val query =
            database.orderByChild("/adFilter/$orderBy").endBefore(filterAds + "_$time").limitToLast(
                ADS_LIMIT
            )

        readNextPageFromDb(query, filterAds, orderBy, readDataCallback)
    }

    fun getAllAnnouncementFromCatFirstPage(
        cat: String,
        filter: String,
        readDataCallback: ReadDataCallback?
    ) {
        val query = if (filter.isEmpty()) {
            database.orderByChild("/adFilter/cat_time").startAt(cat).endAt(cat + "_\uf8ff")
                .limitToLast(
                    ADS_LIMIT
                )
        } else {
            getAllAnnouncementFromCatByFilterFirstPage(cat, filter)
        }
        readDataFromDb(query, readDataCallback)
    }

    private fun getAllAnnouncementFromCatByFilterFirstPage(cat: String, filter: String): Query {
        val orderBy = "cat_" + filter.split("|")[0]
        val filterAds = cat + "_" + filter.split("|")[1]
        return database.orderByChild("/adFilter/$orderBy").startAt(filterAds)
            .endAt(filterAds + "\uf8ff").limitToLast(
            ADS_LIMIT
        )
    }

    fun getAllAnnouncementFromCatNextPage(
        cat: String,
        time: String,
        filter: String,
        readDataCallback: ReadDataCallback?
    ) {
        if (filter.isEmpty()) {
            val query =
                database.orderByChild("/adFilter/cat_time").endBefore(cat + "_" + time).limitToLast(
                    ADS_LIMIT
                )
            readDataFromDb(query, readDataCallback)
        } else {
            getAllAnnouncementFromCatByFilterNextPage(cat, time, filter, readDataCallback)
        }

    }

    private fun getAllAnnouncementFromCatByFilterNextPage(
        cat: String,
        time: String,
        filter: String,
        readDataCallback: ReadDataCallback?
    ) {
        val orderBy = "cat_" + filter.split("|")[0]
        val filterAds = cat + "_" + filter.split("|")[1]
        val query = database.orderByChild("/adFilter/$orderBy").endBefore(filterAds + "_" + time)
            .limitToLast(
                ADS_LIMIT
            )
        readNextPageFromDb(query, filterAds, orderBy, readDataCallback)
    }

    fun deleteAnnouncement(ad: Announcement, listener: FinishWorkListener) {
        if (ad.key == null || ad.uid == null) return
        database.child(ad.key).removeValue().addOnCompleteListener {
            if (it.isSuccessful) listener.onFinish()
        }
/*        database.child(ad.key).child(ad.uid).removeValue().addOnCompleteListener {
            if (it.isSuccessful) listener.onFinish()
        }*/
    }

    private fun readDataFromDb(query: Query, readDataCallback: ReadDataCallback?) {
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            val adArray = ArrayList<Announcement>()
            override fun onDataChange(snapshot: DataSnapshot) {
                for (item in snapshot.children) {

                    var ad: Announcement? = null

                    item.children.forEach {
                        if (ad == null) ad = it.child(AD_NODE).getValue(Announcement::class.java)
                    }
                    val infoItem = item.child(INFO_NODE).getValue(InfoItem::class.java)

                    val favCounter = item.child(FAVS_NODE).childrenCount
                    val isFav = auth.uid?.let {
                        item.child(FAVS_NODE).child(it).getValue(String::class.java)
                    }
                    ad?.isFav = isFav != null
                    ad?.favCounter = favCounter.toString()
                    ad?.viewsCounter = infoItem?.viewsCounter ?: "0"
                    ad?.emailCounter = infoItem?.emailCounter ?: "0"
                    ad?.callsCounter = infoItem?.callsCounter ?: "0"
                    //if (ad != null) adArray.add(ad!!)
                    if (ad != null) adArray.add(ad!!)
                }
                readDataCallback?.readData(adArray)
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun readNextPageFromDb(
        query: Query,
        filter: String,
        orderBy: String,
        readDataCallback: ReadDataCallback?
    ) {
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            val adArray = ArrayList<Announcement>()
            override fun onDataChange(snapshot: DataSnapshot) {
                for (item in snapshot.children) {

                    var ad: Announcement? = null

                    item.children.forEach {
                        if (ad == null) ad = it.child(AD_NODE).getValue(Announcement::class.java)
                    }
                    val infoItem = item.child(INFO_NODE).getValue(InfoItem::class.java)
                    val filterNodeValue = item.child(FILTER_NODE).child(orderBy).value.toString()

                    val favCounter = item.child(FAVS_NODE).childrenCount
                    val isFav = auth.uid?.let {
                        item.child(FAVS_NODE).child(it).getValue(String::class.java)
                    }
                    ad?.isFav = isFav != null
                    ad?.favCounter = favCounter.toString()
                    ad?.viewsCounter = infoItem?.viewsCounter ?: "0"
                    ad?.emailCounter = infoItem?.emailCounter ?: "0"
                    ad?.callsCounter = infoItem?.callsCounter ?: "0"

                    if (ad != null && filterNodeValue.startsWith(filter)) adArray.add(ad!!)
                }
                readDataCallback?.readData(adArray)
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    interface ReadDataCallback {
        fun readData(list: ArrayList<Announcement>)
    }

    interface FinishWorkListener {
        fun onFinish()
    }

    companion object {
        const val AD_NODE = "announcement"
        const val FILTER_NODE = "adFilter"
        const val INFO_NODE = "info"
        const val MAIN_NODE = "main"
        const val FAVS_NODE = "favs"
        const val ADS_LIMIT = 2
    }
}