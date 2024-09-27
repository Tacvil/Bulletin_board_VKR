package com.example.bulletin_board.model

import android.content.Context
import com.example.bulletin_board.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import timber.log.Timber

class DbManager {
    val database = Firebase.database.getReference(MAIN_NODE)
    val firestore = FirebaseFirestore.getInstance()
    val dbStorage = Firebase.storage.getReference(MAIN_NODE)
    val auth = Firebase.auth

    fun saveToken(token: String) {
        if (auth.uid != null) {
            val userRef = firestore.collection(USER_NODE).document(auth.uid ?: "empty")
            // Создаем HashMap с полем "token"
            val tokenData =
                hashMapOf(
                    "token" to token,
                )
            // Обновляем данные пользователя в Firestore, добавляя поле "token"
            userRef
                .set(
                    tokenData,
                    SetOptions.merge(),
                ) // Используем SetOptions.merge(), чтобы добавить поле без удаления существующих данных
                .addOnSuccessListener {
                    println("Токен успешно сохранен для пользователя с ID: ${auth.uid}")
                }.addOnFailureListener { e ->
                    println("Ошибка при сохранении токена для пользователя с ID: ${auth.uid}, ошибка: $e")
                }
        }
    }

    fun publishAnnouncement1(
        ad: Ad,
        finishListener: FinishWorkListener,
    ) {
        if (auth.uid != null) {
            val userRef = firestore.collection(MAIN_NODE).document(ad.key ?: "empty")
            userRef
                .set(ad)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        finishListener.onFinish(true)
                    } else {
                        finishListener.onFinish(false)
                    }
                }
        }
    }

    fun adViewed(ad: Ad) {
        var counter = ad.viewsCounter.toInt()
        counter++

        Timber.tag("DBCounter").d("Counter = %s", counter)

        val dataToUpdate =
            hashMapOf<String, Any>(
                "viewsCounter" to counter,
            )

        if (auth.uid != null) {
            firestore
                .collection(MAIN_NODE)
                .document(ad.key ?: "empty")
                .update(dataToUpdate)
        }
    }

    fun onFavClick(
        ad: Ad,
        listener: FinishWorkListener,
    ) {
        if (ad.isFav) removeFromFavs(ad, listener) else addToFavs(ad, listener)
    }

    private fun addToFavs(
        ad: Ad,
        listener: FinishWorkListener,
    ) {
        ad.key?.let {
            auth.uid?.let { uid ->
                firestore
                    .collection(MAIN_NODE)
                    .document(it)
                    .update("favUids", FieldValue.arrayUnion(uid))
                    .addOnCompleteListener {
                        if (it.isSuccessful) listener.onFinish(true)
                    }
            }
        }
    }

    private fun removeFromFavs(
        ad: Ad,
        listener: FinishWorkListener,
    ) {
        ad.key?.let {
            auth.uid?.let { uid ->
                firestore
                    .collection(MAIN_NODE)
                    .document(it)
                    .update("favUids", FieldValue.arrayRemove(uid))
                    .addOnCompleteListener {
                        if (it.isSuccessful) listener.onFinish(true)
                    }
            }
        }
    }

    fun getMyAnnouncement(readDataCallback: ReadDataCallback?) {
        val query =
            firestore
                .collection(MAIN_NODE)
                .whereEqualTo("uid", auth.uid)
                .orderBy("time", Query.Direction.DESCENDING)
        readDataFromDb(query, readDataCallback)
    }

    fun getMyFavs(readDataCallback: ReadDataCallback?) {
        val query =
            auth.uid?.let {
                firestore
                    .collection(
                        MAIN_NODE,
                    ).whereArrayContains("favUids", it)
                    .orderBy("time", Query.Direction.DESCENDING)
            }
        if (query != null) {
            readDataFromDb(query, readDataCallback)
        }
    }

    fun deleteAnnouncement(
        ad: Ad,
        listener: FinishWorkListener,
    ) {
        if (ad.key == null || ad.uid == null) return

        firestore
            .collection(MAIN_NODE)
            .document(ad.key)
            .delete()
            .addOnSuccessListener {
                // Успешно удалено
                listener.onFinish(true)
            }.addOnFailureListener { e ->
                // Обработка ошибок при удалении
                listener.onFinish(false)
                Timber.tag("Dbmanager").d("Exeption -> " + e)
            }
    }

    private fun getAllAnnouncementsByFilter(
        context: Context,
        filter: MutableMap<String, String>,
        time: String? = null,
        viewsCounter: Int? = null,
        lastDocumentAds: QueryDocumentSnapshot? = null,
    ): Query {
        var queryDB =
            firestore
                .collection(MAIN_NODE)
                .whereEqualTo("published", true)

        if (filter["keyWords"]?.isNotEmpty() == true) {
            queryDB = queryDB.whereArrayContains("keyWords", filter["keyWords"]!!)
        }
        if (filter["country"]?.isNotEmpty() == true) {
            queryDB = queryDB.whereEqualTo("country", filter["country"])
        }
        if (filter["city"]?.isNotEmpty() == true) {
            queryDB = queryDB.whereEqualTo("city", filter["city"])
        }
        if (filter["index"]?.isNotEmpty() == true) {
            queryDB = queryDB.whereEqualTo("index", filter["index"])
        }
        if (!filter["category"].isNullOrEmpty() && filter["category"] != context.getString(R.string.def)) {
            queryDB = queryDB.whereEqualTo("category", filter["category"])
        }
        /*when (filter["withSend"]) {
            SortOption.WITH_SEND.id -> queryDB = queryDB.whereEqualTo("withSend", SortOption.WITH_SEND.id)
            SortOption.WITHOUT_SEND.id -> queryDB = queryDB.whereEqualTo("withSend", SortOption.WITHOUT_SEND.id)
            else -> {}
        }

        if (filter["price_from"]?.isNotEmpty() == true || filter["price_to"]?.isNotEmpty() == true) {
            try {
                val priceFrom = filter["price_from"]?.toIntOrNull() ?: 0
                val priceTo = filter["price_to"]?.toIntOrNull() ?: Int.MAX_VALUE
                queryDB =
                    queryDB
                        .whereGreaterThanOrEqualTo("price", priceFrom)
                        .whereLessThanOrEqualTo("price", priceTo)
            } catch (e: Exception) {
                Timber.tag("DbManager").e(e, "Ошибка преобразования цены")
            }
        }

        queryDB =
            when (filter["orderBy"]) {
                SortOption.BY_NEWEST.id -> {
                    if (time != null) queryDB.whereLessThan("time", time)
                    queryDB.orderBy("time", Query.Direction.DESCENDING)
                }
                SortOption.BY_POPULARITY.id -> {
                    if (viewsCounter != null) queryDB.whereLessThanOrEqualTo("viewsCounter", viewsCounter)
                    queryDB
                        .orderBy("viewsCounter", Query.Direction.DESCENDING)
                        .orderBy("key", Query.Direction.DESCENDING)
                }
                SortOption.BY_PRICE_ASC.id -> {
                    queryDB
                        .orderBy("price", Query.Direction.ASCENDING)
                        .orderBy("key", Query.Direction.ASCENDING)
                }
                SortOption.BY_PRICE_DESC.id -> {
                    queryDB
                        .orderBy("price", Query.Direction.DESCENDING)
                        .orderBy("key", Query.Direction.DESCENDING)
                }
                else -> {
                    if (time != null) queryDB.whereLessThan("time", time)
                    queryDB.orderBy("time", Query.Direction.DESCENDING)
                }
            }

        if (lastDocumentAds != null) {
            when (filter["orderBy"]) {
                SortOption.BY_PRICE_ASC.id, SortOption.BY_PRICE_DESC.id -> {
                    val lastPrice = lastDocumentAds.getLong("price")?.toInt()
                    val lastKey = lastDocumentAds.get("key") as? String
                    if (lastPrice != null && lastKey != null) {
                        queryDB = queryDB.startAfter(lastPrice, lastKey)
                    }
                }
                SortOption.BY_POPULARITY.id -> {
                    val lastViewsCounter = lastDocumentAds.getLong("viewsCounter")?.toInt()
                    val lastKey = lastDocumentAds.get("key") as? String
                    if (lastViewsCounter != null && lastKey != null) {
                        queryDB = queryDB.startAfter(lastViewsCounter, lastKey)
                    }
                }
                SortOption.BY_NEWEST.id -> {
                    val lastTime = lastDocumentAds.get("time") as? String
                    Timber.tag("DbManager").d("lastDocumentAds = %s", lastDocumentAds.data)
                    if (lastTime != null) {
                        queryDB = queryDB.startAfter(lastTime)
                    }
                }
                else -> {
                    val lastTime = lastDocumentAds.get("time") as? String
                    if (lastTime != null) {
                        queryDB = queryDB.startAfter(lastTime)
                    }
                }
            }
        }*/

        return queryDB.limit(ADS_LIMIT.toLong())
    }

    fun getAllAnnouncements(
        context: Context,
        filter: MutableMap<String, String>,
        time: String? = null,
        viewsCounter: Int? = null,
        lastDocumentAds: QueryDocumentSnapshot? = null,
        readDataCallback: ReadDataCallback?,
    ) {
        val query = getAllAnnouncementsByFilter(context, filter, time, viewsCounter, lastDocumentAds)
        readDataFromDb(query, readDataCallback)
    }

    private fun readDataFromDb(
        query: Query,
        readDataCallback: ReadDataCallback?,
    ) {
        query.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val adArray = ArrayList<Ad>()
                val lastDocument = task.result!!.lastOrNull()

                for (document in task.result!!) {
                    try {
                        val ad = document.toObject(Ad::class.java)
                        val favUids = document.get("favUids") as? List<String>

                        ad.isFav = auth.uid?.let { favUids?.contains(it) } ?: false
                        ad.favCounter = favUids?.size.toString()

                        adArray.add(ad)
                    } catch (e: Exception) {
                        Timber.tag("DbManager").e(e, "Ошибка преобразования данных объявления")
                    }
                }

                readDataCallback?.readData(adArray, lastDocument)
            } else {
                Timber.tag("DbManager").e("Ошибка при получении данных: %s", task.exception)
                readDataCallback?.onError(task.exception ?: Exception("Unknown error")) // Вызываем onError
            }
            readDataCallback?.onComplete()
        }
    }

    interface ReadDataCallback {
        fun readData(
            list: ArrayList<Ad>,
            lastDocument: QueryDocumentSnapshot?,
        )

        fun onComplete()

        fun onError(e: Exception)
    }

    interface FinishWorkListener {
        fun onFinish(isDone: Boolean)
    }

    companion object {
        const val AD_NODE = "announcement"
        const val FILTER_NODE = "adFilter"
        const val INFO_NODE = "info"
        const val MAIN_NODE = "main"
        const val USER_NODE = "users"
        const val FAVS_NODE = "favs"
        const val ADS_LIMIT = 2
    }
}
