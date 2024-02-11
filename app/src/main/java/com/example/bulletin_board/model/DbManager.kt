package com.example.bulletin_board.model

import android.util.Log
import com.example.bulletin_board.utils.FilterManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.firebase.firestore.Query

class DbManager {
    val database = Firebase.database.getReference(MAIN_NODE)
    val firestore = FirebaseFirestore.getInstance()
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
                        finishListener.onFinish(it.isSuccessful)
                    }
            }
    }

    fun publishAnnouncement1(announcement: Announcement, finishListener: FinishWorkListener) {
        if (auth.uid != null) {
            val userRef = firestore.collection(MAIN_NODE).document(announcement.key ?: "empty")
            userRef.set(announcement)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        finishListener.onFinish(true)
                    } else {
                        finishListener.onFinish(false)
                    }
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
                        if (it.isSuccessful) listener.onFinish(true)
                    }
            }
        }
    }

    private fun removeFromFavs(ad: Announcement, listener: FinishWorkListener) {
        ad.key?.let {
            auth.uid?.let { uid ->
                database.child(it).child(FAVS_NODE)
                    .child(uid).removeValue().addOnCompleteListener {
                        if (it.isSuccessful) listener.onFinish(true)
                    }
            }
        }
    }

    /*    fun getMyAnnouncement(readDataCallback: ReadDataCallback?) {
            val query = database.orderByChild(auth.uid + "/announcement/uid").equalTo(auth.uid)
            readDataFromDb(query, readDataCallback)
        }

        fun getMyFavs(readDataCallback: ReadDataCallback?) {
            val query = database.orderByChild("/favs/${auth.uid}").equalTo(auth.uid)
            readDataFromDb(query, readDataCallback)
        }*/

    fun getAllAnnouncementFirstPage1(filter: String, readDataCallback: ReadDataCallback?) {
        val query = if (filter.isEmpty()) {
            firestore.collection(MAIN_NODE).orderBy("time", Query.Direction.ASCENDING)
                .limit(ADS_LIMIT.toLong())
        } else {
            getAllAnnouncementByFilterFirstPage1(filter)
        }
        //val query = firestore.collection(MAIN_NODE).orderBy("time", Query.Direction.ASCENDING)

        readDataFromDb1(query, readDataCallback)
    }

    private fun getAllAnnouncementByFilterFirstPage1(filter: String): Query {
        val orderBy = filter.split("|")[0] //time:
        val filterAds = filter.split("|")[1] //123123123

        return firestore.collection("/adFilter/$orderBy").startAt(filterAds)
            .endAt("$filterAds\uf8ff")
    }

    /*    fun getAllAnnouncementFirstPage(filter: String, readDataCallback: ReadDataCallback?) {
            val query = if (filter.isEmpty()) {
                database.orderByChild("/adFilter/time").limitToLast(
                    ADS_LIMIT
                ) // последние 2 элемента в бд по времени потом реверс делаю (1 2 3 4 5 6 ) - берем 5 и 6 последних по времени

            } else {
                getAllAnnouncementByFilterFirstPage(filter)
            }
            readDataFromDb(query, readDataCallback)
        }*/

    /*    private fun getAllAnnouncementByFilterFirstPage(filter: String): Query {
            val orderBy = filter.split("|")[0]
            val filterAds = filter.split("|")[1]
            val db = FirebaseFirestore.getInstance()
            val data = hashMapOf(
                "название_поля1" to "значение1",
                "название_поля2" to "значение2",
                // добавьте другие поля и значения, как необходимо
            )

            db.collection("ваша_коллекция").add(data)
                .addOnSuccessListener { documentReference ->
                    Log.d("TAG", "Данные успешно добавлены с ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.w("TAG", "Ошибка при добавлении данных", e)
                }
            return database.orderByChild("/adFilter/$orderBy").startAt(filterAds).endAt("$filterAds\uf8ff")
        }*/

        fun getAllAnnouncementNextPage1(
        time: String,
        filter: String,
        readDataCallback: ReadDataCallback?
    ) {
/*        if (filter.isEmpty()) {
            val query = database.orderByChild("/adFilter/time").endBefore(time).limitToLast(
                ADS_LIMIT
            )
            readDataFromDb(query, readDataCallback)
        } else {
            getAllAnnouncementByFilterNextPage(filter, time, readDataCallback)
        }*/
            Log.d("DbManager", "time: $time")
            val query = firestore.collection(MAIN_NODE).whereGreaterThan("time", time).limit(
                ADS_LIMIT.toLong())
            readDataFromDb1(query, readDataCallback)
    }

    /*    fun getAllAnnouncementNextPage(
            time: String,
            filter: String,
            readDataCallback: ReadDataCallback?
        ) {
            Log.d("DbManagerGAABFNP", "time: $time")
            if (filter.isEmpty()) {
                val query = database.orderByChild("/adFilter/time").endBefore(time).limitToLast(
                    ADS_LIMIT
                )
                readDataFromDb(query, readDataCallback)
            } else {
                getAllAnnouncementByFilterNextPage(filter, time, readDataCallback)
            }
        }*/

    /*    private fun getAllAnnouncementByFilterNextPage(
            filter: String,
            time: String,
            readDataCallback: ReadDataCallback?
        ) {
            val orderBy = filter.split("|")[0]
            val filterAds = filter.split("|")[1]
            Log.d("DbManagerGAABFNP", "orderBy: $orderBy")
            Log.d("DbManagerGAABFNP", "filterAds: $filterAds")
            Log.d("DbManagerGAABFNP", "time: $time")
            val query =
                database.orderByChild("/adFilter/$orderBy").endBefore("${filterAds}_$time").limitToLast(
                    ADS_LIMIT
                )

            readNextPageFromDb(query, filterAds, orderBy, readDataCallback)
        }*/

    /*    fun getAllAnnouncementFromCatFirstPage(
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
        }*/

    /*    private fun getAllAnnouncementFromCatByFilterFirstPage(cat: String, filter: String): Query {
            val orderBy = "cat_" + filter.split("|")[0]
            val filterAds = cat + "_" + filter.split("|")[1]
            return database.orderByChild("/adFilter/$orderBy").startAt(filterAds)
                .endAt(filterAds + "\uf8ff").limitToLast(
                ADS_LIMIT
            )
        }*/

    /*    fun getAllAnnouncementFromCatNextPage(
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

        }*/

    /*
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
    */

    fun deleteAnnouncement(ad: Announcement, listener: FinishWorkListener) {
        if (ad.key == null || ad.uid == null) return
        database.child(ad.key).removeValue().addOnCompleteListener {
            if (it.isSuccessful) listener.onFinish(true)
        }
        /*        database.child(ad.key).child(ad.uid).removeValue().addOnCompleteListener {
                    if (it.isSuccessful) listener.onFinish()
                }*/
    }

    private fun readDataFromDb1(query: Query, readDataCallback: ReadDataCallback?) {
        query.get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val adArray = ArrayList<Announcement>()
                    Log.d("DbManager", "Результаты запроса: ${task.result}")
                    if (!task.result!!.isEmpty) {
                        Log.d(
                            "DbManager",
                            "Количество документов в результате: ${task.result!!.size()}"
                        )
                        for (document in task.result!!) {
                            Log.d("DbManager", "Данные документа: ${document.data}")
                        }
                    } else {
                        Log.d("DbManager", "Результат запроса пуст.")
                    }

                    for (document in task.result!!) {

                        val adData = document.data as Map<*, *>?
                        val ad = adData?.let {
                            document.toObject(Announcement::class.java)
                        }
                        Log.d("DbManager", "adData: $adData")
                        Log.d("DbManager", "ad: $ad")

                        // Если есть дополнительные поля в вашем документе, вы можете получить их аналогичным образом

                        val infoItem = document.data["info"] as InfoItem?
                        /*                        val favCounter = document.reference.collection("favs").document().get().result

                                                ad?.isFav = ad?.uid?.let {
                                                    document.reference.collection("favs").document(it).get().isSuccessful
                                                } ?: false
                                                ad?.favCounter = favCounter?.toString() ?: "0"*/
                        ad?.viewsCounter = infoItem?.viewsCounter ?: "0"
                        ad?.emailCounter = infoItem?.emailCounter ?: "0"
                        ad?.callsCounter = infoItem?.callsCounter ?: "0"

                        ad?.let { adArray.add(it) }
                    }

                    readDataCallback?.readData(adArray)
                } else {
                    Log.e("DbManager", "Ошибка при получении данных: ${task.exception}")
                    // Обработка ошибки
                }
            }
    }

    /*
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
    //                    Log.d("DbManager", "adArray: $adArray")
    //                    Log.d("DbManager", "filter: $filter")
                    }
                    readDataCallback?.readData(adArray)
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        }
    */

    interface ReadDataCallback {
        fun readData(list: ArrayList<Announcement>)
    }

    interface FinishWorkListener {
        fun onFinish(isDone: Boolean)
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