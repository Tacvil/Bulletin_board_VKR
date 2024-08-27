package com.example.bulletin_board.model

import android.content.Context
import android.util.Log
import com.example.bulletin_board.R
import com.example.bulletin_board.utils.FilterManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.json.JSONObject

enum class SortOption(
    val id: String,
) {
    BY_NEWEST("byNewest"),
    BY_POPULARITY("byPopularity"),
    BY_PRICE_ASC("byPriceAsc"),
    BY_PRICE_DESC("byPriceDesc"),
}

class DbManager {
    val database = Firebase.database.getReference(MAIN_NODE)
    private val firestore = FirebaseFirestore.getInstance()
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

    fun publishAnnouncement(
        announcement: Announcement,
        finishListener: FinishWorkListener,
    ) {
        if (auth.uid != null) {
            database
                .child(announcement.key ?: "empty")
                .child(auth.uid!!)
                .child(AD_NODE)
                .setValue(announcement)
                .addOnCompleteListener {
                    val adFilter = FilterManager.createFilter(announcement)
                    database
                        .child(announcement.key ?: "empty")
                        .child(FILTER_NODE)
                        .setValue(adFilter)
                        .addOnCompleteListener {
                            finishListener.onFinish(it.isSuccessful)
                        }
                }
        }
    }

    fun publishAnnouncement1(
        announcement: Announcement,
        finishListener: FinishWorkListener,
    ) {
        if (auth.uid != null) {
            val userRef = firestore.collection(MAIN_NODE).document(announcement.key ?: "empty")
            userRef
                .set(announcement)
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

        Log.d("DBCounter", "Counter = $counter")

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
        ad: Announcement,
        listener: FinishWorkListener,
    ) {
        if (ad.isFav) removeFromFavs(ad, listener) else addToFavs(ad, listener)
    }

    private fun addToFavs(
        ad: Announcement,
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
        ad: Announcement,
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
        readDataFromDb(query, readDataCallback, null)
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
            readDataFromDb(query, readDataCallback, null)
        }
    }

    fun deleteAnnouncement(
        ad: Announcement,
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
                Log.d("Dbmanager", "Exeption -> $e")
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
        when (filter["withSend"]) {
            "С отправкой", "Без отправки" -> queryDB = queryDB.whereEqualTo("withSend", filter["withSend"])
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
                Log.e("DbManager", "Ошибка преобразования цены", e)
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
                    Log.d("DbManager", "lastViewsCounter: $lastViewsCounter")
                    val jsonObject = lastDocumentAds.data.let { JSONObject(it) }
                    Log.d("DbManager", "lastDocumentAds: $jsonObject")
                    val lastKey = lastDocumentAds.get("key") as? String
                    Log.d("DbManager", "lastKey: $lastKey")
                    if (lastViewsCounter != null && lastKey != null) {
                        queryDB = queryDB.startAfter(lastViewsCounter, lastKey)
                    }
                }
/*                SortOption.BY_POPULARITY.id -> {
                    queryDB =
                        queryDB
                            .whereLessThanOrEqualTo("viewsCounter", viewsCounter)
                            .orderBy("viewsCounter", Query.Direction.DESCENDING)
                            .orderBy("key", Query.Direction.DESCENDING)
                            .startAfter(
                                lastDocumentAds?.get("viewsCounter") ?: "",
                                lastDocumentAds?.get("key") ?: "",
                            ).limit(ADS_LIMIT.toLong())
                }*/
                SortOption.BY_NEWEST.id -> {
                    val lastTime = lastDocumentAds.get("time") as? String
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
        }

        return queryDB.limit(ADS_LIMIT.toLong())
    }

    fun getAllAnnouncements(
        context: Context,
        filter: MutableMap<String, String>,
        time: String? = null,
        viewsCounter: Int? = null,
        lastDocumentAds: QueryDocumentSnapshot? = null,
        readDataCallback: ReadDataCallback?,
        onComplete: () -> Unit,
    ) {
        val query = getAllAnnouncementsByFilter(context, filter, time, viewsCounter, lastDocumentAds)
        readDataFromDb(query, readDataCallback, onComplete)
    }

    private fun readDataFromDb(
        query: Query,
        readDataCallback: ReadDataCallback?,
        onComplete: (() -> Unit?)?,
    ) {
        query.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val adArray = ArrayList<Announcement>()
                val lastDocument = task.result!!.lastOrNull()

                for (document in task.result!!) {
                    try {
                        val ad = document.toObject(Announcement::class.java)
                        val favUids = document.get("favUids") as? List<String>

                        ad.isFav = auth.uid?.let { favUids?.contains(it) } ?: false
                        ad.favCounter = favUids?.size.toString()

                        adArray.add(ad)
                    } catch (e: Exception) {
                        Log.e("DbManager", "Ошибка преобразования данных объявления", e)
                    }
                }

                readDataCallback?.readData(adArray, lastDocument)
            } else {
                Log.e("DbManager", "Ошибка при получении данных: ${task.exception}")
            }
            onComplete?.invoke()
        }
    }

   /* fun getAllAnnouncementFirstPage1(
        context: Context,
        filter: MutableMap<String, String>,
        readDataCallback: ReadDataCallback?,
    ) {
        val query =
            if (filter.isEmpty()) {
                firestore
                    .collection(MAIN_NODE)
                    .orderBy("time", Query.Direction.ASCENDING)
                    .limit(ADS_LIMIT.toLong())
            } else {
                getAllAnnouncementByFilterFirstPage1(context, filter)
            }

        readDataFromDb1(query, readDataCallback, null)
    }

    private fun getAllAnnouncementByFilterFirstPage1(
        context: Context,
        filter: MutableMap<String, String>,
    ): Query {
        var queryDB: Query = firestore.collection(MAIN_NODE)

        queryDB = queryDB.whereEqualTo("published", true)

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
        if (filter["category"]?.isNotEmpty() == true && filter["category"] != context.getString(R.string.def)) {
            queryDB = queryDB.whereEqualTo("category", filter["category"])
        }
        when (filter["withSend"]?.isNotEmpty() == true) {
            (filter["withSend"] == "Не важно") -> {}
            (filter["withSend"] == "С отправкой") -> {
                queryDB = queryDB.whereEqualTo("withSend", filter["withSend"])
            }

            (filter["withSend"] == "Без отправки") -> {
                queryDB = queryDB.whereEqualTo("withSend", filter["withSend"])
            }

            else -> {
                Log.d("DbManager_GAABFFP", "when -> else")
            }
        }
        if (filter["price_from"]?.isNotEmpty() == true || filter["price_to"]?.isNotEmpty() == true) {
            if (filter["price_from"]?.isNotEmpty() == true && filter["price_to"]?.isNotEmpty() == true) {
                if (filter["orderBy"]?.isNotEmpty() == true) {
                    when (filter["orderBy"]) {
                        SortOption.BY_PRICE_ASC.id -> {
                            queryDB =
                                queryDB
                                    .whereGreaterThanOrEqualTo(
                                        "price",
                                        filter["price_from"]?.toInt()!!,
                                    ).whereLessThanOrEqualTo("price", filter["price_to"]?.toInt()!!)
                                    .orderBy("price", Query.Direction.ASCENDING)
                                    .limit(ADS_LIMIT.toLong())
                        }

                        SortOption.BY_PRICE_DESC.id -> {
                            queryDB =
                                queryDB
                                    .whereGreaterThanOrEqualTo(
                                        "price",
                                        filter["price_from"]?.toInt()!!,
                                    ).whereLessThanOrEqualTo("price", filter["price_to"]?.toInt()!!)
                                    .orderBy("price", Query.Direction.DESCENDING)
                                    .limit(ADS_LIMIT.toLong())
                        }

                        else -> {
                            Log.d("DbManager_GAABFFP", "when orderBy PRICE1 -> else")
                        }
                    }
                } else {
                    Log.d("DbManager_GAABFFP", "filter[\\\"orderBy\\\"] is empty or null")
                }
            } else {
                if (filter["price_from"]?.isNotEmpty() == true) {
                    if (filter["orderBy"]?.isNotEmpty() == true) {
                        when (filter["orderBy"]) {
                            SortOption.BY_PRICE_ASC.id -> {
                                queryDB =
                                    queryDB
                                        .whereGreaterThanOrEqualTo(
                                            "price",
                                            filter["price_from"]?.toInt()!!,
                                        ).orderBy("price", Query.Direction.ASCENDING)
                                        .limit(ADS_LIMIT.toLong())
                            }

                            SortOption.BY_PRICE_DESC.id -> {
                                queryDB =
                                    queryDB
                                        .whereGreaterThanOrEqualTo(
                                            "price",
                                            filter["price_from"]?.toInt()!!,
                                        ).orderBy("price", Query.Direction.DESCENDING)
                                        .limit(ADS_LIMIT.toLong())
                            }

                            else -> {
                                Log.d("DbManager_GAABFFP", "when orderBy PRICE2 -> else")
                            }
                        }
                    } else {
                        Log.d("DbManager_GAABFFP", "filter[\\\"orderBy\\\"] is empty or null")
                    }
                } else {
                    if (filter["orderBy"]?.isNotEmpty() == true) {
                        when (filter["orderBy"]) {
                            SortOption.BY_PRICE_ASC.id -> {
                                queryDB =
                                    queryDB
                                        .whereLessThanOrEqualTo(
                                            "price",
                                            filter["price_to"]?.toInt()!!,
                                        ).orderBy("price", Query.Direction.ASCENDING)
                                        .limit(ADS_LIMIT.toLong())
                            }

                            SortOption.BY_PRICE_DESC.id -> {
                                queryDB =
                                    queryDB
                                        .whereLessThanOrEqualTo(
                                            "price",
                                            filter["price_to"]?.toInt()!!,
                                        ).orderBy("price", Query.Direction.DESCENDING)
                                        .limit(ADS_LIMIT.toLong())
                            }

                            else -> {
                                Log.d("DbManager_GAABFFP", "when orderBy PRICE3 -> else")
                            }
                        }
                    } else {
                        Log.d("DbManager_GAABFFP", "filter[\\\"orderBy\\\"] is empty or null")
                    }
                }
            }
        } else {
            if (filter["orderBy"]?.isNotEmpty() == true) {
                when (filter["orderBy"]) {
                    SortOption.BY_NEWEST.id -> {
                        queryDB =
                            queryDB
                                .orderBy("time", Query.Direction.DESCENDING)
                                .limit(ADS_LIMIT.toLong())
                    }

                    SortOption.BY_POPULARITY.id -> {
                        queryDB =
                            queryDB
                                .orderBy("viewsCounter", Query.Direction.DESCENDING)
                                .limit(ADS_LIMIT.toLong())
                    }

                    SortOption.BY_PRICE_ASC.id -> {
                        queryDB =
                            queryDB
                                .orderBy("price", Query.Direction.ASCENDING)
                                .limit(ADS_LIMIT.toLong())
                    }

                    SortOption.BY_PRICE_DESC.id -> {
                        queryDB =
                            queryDB
                                .orderBy("price", Query.Direction.DESCENDING)
                                .limit(ADS_LIMIT.toLong())
                    }

                    else -> {
                        Log.d("DbManager_GAABFFP", "when orderBy1 -> else")
                    }
                }
            } else {
                Log.d("DbManager_GAABFFP", "filter[\\\"orderBy\\\"] is empty or null")
            }
        }

        return queryDB
    }

    fun getAllAnnouncementNextPage1(
        context: Context,
        time: String,
        price: Int?,
        viewsCounter: Int,
        lastDocumentAds: QueryDocumentSnapshot?,
        filter: MutableMap<String, String>,
        readDataCallback: ReadDataCallback?,
        onComplete: () -> Unit,
    ) {
        if (filter.isEmpty()) {
            val query =
                firestore.collection(MAIN_NODE).whereGreaterThan("time", time).limit(
                    ADS_LIMIT.toLong(),
                )
            readDataFromDb1(query, readDataCallback, onComplete)
        } else {
            getAllAnnouncementByFilterNextPage1(
                context,
                filter,
                time,
                price,
                viewsCounter,
                lastDocumentAds,
                readDataCallback,
                onComplete,
            )
        }
    }

    private fun getAllAnnouncementByFilterNextPage1(
        context: Context,
        filter: MutableMap<String, String>,
        time: String,
        price: Int?,
        viewsCounter: Int,
        lastDocumentAds: QueryDocumentSnapshot?,
        readDataCallback: ReadDataCallback?,
        onComplete: () -> Unit,
    ) {
        var queryDB: Query = firestore.collection(MAIN_NODE)

        queryDB = queryDB.whereEqualTo("published", true)

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
        if (filter["category"]?.isNotEmpty() == true && filter["category"] != context.getString(R.string.def)) {
            queryDB = queryDB.whereEqualTo("category", filter["category"])
        }
        when (filter["withSend"]?.isNotEmpty() == true) {
            (filter["withSend"] == "Не важно") -> {}
            (filter["withSend"] == "С отправкой") -> {
                queryDB = queryDB.whereEqualTo("withSend", filter["withSend"])
            }

            (filter["withSend"] == "Без отправки") -> {
                queryDB = queryDB.whereEqualTo("withSend", filter["withSend"])
            }

            else -> {
                Log.d("DbManager_GAABFFP", "when -> else")
            }
        }
        if (filter["price_from"]?.isNotEmpty() == true || filter["price_to"]?.isNotEmpty() == true) {
            if (filter["price_from"]?.isNotEmpty() == true && filter["price_to"]?.isNotEmpty() == true) {
                if (filter["orderBy"]?.isNotEmpty() == true) {
                    when (filter["orderBy"]) {
                        SortOption.BY_PRICE_ASC.id -> {
                            queryDB =
                                queryDB
                                    .whereGreaterThanOrEqualTo("price", price!!)
                                    .whereLessThanOrEqualTo("price", filter["price_to"]?.toInt()!!)
                                    .orderBy("price", Query.Direction.ASCENDING)
                                    .orderBy("key", Query.Direction.ASCENDING)
                                    .startAfter(
                                        lastDocumentAds?.get("price") ?: "",
                                        lastDocumentAds?.get("key") ?: "",
                                    ).limit(ADS_LIMIT.toLong())
                        }

                        SortOption.BY_PRICE_DESC.id -> {
                            queryDB =
                                queryDB
                                    .whereGreaterThanOrEqualTo("price", price!!)
                                    .whereLessThanOrEqualTo("price", filter["price_to"]?.toInt()!!)
                                    .orderBy("price", Query.Direction.DESCENDING)
                                    .orderBy("key", Query.Direction.ASCENDING)
                                    .startAfter(
                                        lastDocumentAds?.get("price") ?: "",
                                        lastDocumentAds?.get("key") ?: "",
                                    ).limit(ADS_LIMIT.toLong())
                        }

                        else -> {
                            Log.d("DbManager_GAABFFP", "when orderBy PRICE4 -> else")
                        }
                    }
                } else {
                    Log.d("DbManager_GAABFFP", "filter[\\\"orderBy\\\"] is empty or null")
                }
            } else {
                if (filter["price_from"]?.isNotEmpty() == true) {
                    if (filter["orderBy"]?.isNotEmpty() == true) {
                        when (filter["orderBy"]) {
                            SortOption.BY_PRICE_ASC.id -> {
                                queryDB =
                                    queryDB
                                        .whereGreaterThanOrEqualTo("price", price!!)
                                        .orderBy("price", Query.Direction.ASCENDING)
                                        .orderBy("key", Query.Direction.ASCENDING)
                                        .startAfter(
                                            lastDocumentAds?.get("price") ?: "",
                                            lastDocumentAds?.get("key") ?: "",
                                        ).limit(ADS_LIMIT.toLong())
                            }

                            SortOption.BY_PRICE_DESC.id -> {
                                queryDB =
                                    queryDB
                                        .whereGreaterThanOrEqualTo("price", price!!)
                                        .orderBy("price", Query.Direction.DESCENDING)
                                        .orderBy("key", Query.Direction.ASCENDING)
                                        .startAfter(
                                            lastDocumentAds?.get("price") ?: "",
                                            lastDocumentAds?.get("key") ?: "",
                                        ).limit(ADS_LIMIT.toLong())
                            }

                            else -> {
                                Log.d("DbManager_GAABFFP", "when orderBy PRICE5 -> else")
                            }
                        }
                    } else {
                        Log.d("DbManager_GAABFFP", "filter[\\\"orderBy\\\"] is empty or null")
                    }
                } else {
                    if (filter["orderBy"]?.isNotEmpty() == true) {
                        when (filter["orderBy"]) {
                            SortOption.BY_PRICE_ASC.id -> {
                                queryDB =
                                    queryDB
                                        .whereGreaterThanOrEqualTo("price", price!!)
                                        .whereLessThanOrEqualTo("price", filter["price_to"]!!)
                                        .orderBy("price", Query.Direction.ASCENDING)
                                        .orderBy("key", Query.Direction.ASCENDING)
                                        .startAfter(
                                            lastDocumentAds?.get("price") ?: "",
                                            lastDocumentAds?.get("key") ?: "",
                                        ).limit(ADS_LIMIT.toLong())
                            }

                            SortOption.BY_PRICE_DESC.id -> {
                                queryDB =
                                    queryDB
                                        .whereLessThanOrEqualTo("price", price!!)
                                        .orderBy("price", Query.Direction.DESCENDING)
                                        .orderBy("key", Query.Direction.ASCENDING)
                                        .startAfter(
                                            lastDocumentAds?.get("price") ?: "",
                                            lastDocumentAds?.get("key") ?: "",
                                        ).limit(ADS_LIMIT.toLong())
                            }

                            else -> {
                                Log.d("DbManager_GAABFFP", "when orderBy PRICE6 -> else")
                            }
                        }
                    } else {
                        Log.d("DbManager_GAABFFP", "filter[\\\"orderBy\\\"] is empty or null")
                    }
                }
            }
        } else if (filter["orderBy"]?.isNotEmpty() == true) {
            when (filter["orderBy"]) {
                SortOption.BY_NEWEST.id -> {
                    queryDB =
                        queryDB
                            .whereLessThan("time", time)
                            .orderBy("time", Query.Direction.DESCENDING)
                            .limit(ADS_LIMIT.toLong())
                }

                SortOption.BY_POPULARITY.id -> {
                    queryDB =
                        queryDB
                            .whereLessThanOrEqualTo("viewsCounter", viewsCounter)
                            .orderBy("viewsCounter", Query.Direction.DESCENDING)
                            .orderBy("key", Query.Direction.DESCENDING)
                            .startAfter(
                                lastDocumentAds?.get("viewsCounter") ?: "",
                                lastDocumentAds?.get("key") ?: "",
                            ).limit(ADS_LIMIT.toLong())
                }

                SortOption.BY_PRICE_ASC.id -> {
                    queryDB =
                        queryDB
                            .whereGreaterThanOrEqualTo("price", price!!)
                            .orderBy("price", Query.Direction.ASCENDING)
                            .orderBy("key", Query.Direction.ASCENDING)
                            .startAfter(
                                lastDocumentAds?.get("price") ?: "",
                                lastDocumentAds?.get("key") ?: "",
                            ).limit(ADS_LIMIT.toLong())
                }

                SortOption.BY_PRICE_DESC.id -> {
                    queryDB =
                        queryDB
                            .whereLessThanOrEqualTo("price", price!!)
                            .orderBy("price", Query.Direction.DESCENDING)
                            .orderBy("key", Query.Direction.DESCENDING)
                            .startAfter(
                                lastDocumentAds?.get("price") ?: "",
                                lastDocumentAds?.get("key") ?: "",
                            ).limit(ADS_LIMIT.toLong())
                }

                else -> {
                    Log.d("DbManager_GAABFFP", "when orderBy21 -> else")
                }
            }
        } else {
            queryDB.whereLessThan("time", time).orderBy("time", Query.Direction.DESCENDING)
        }

        readDataFromDb1(queryDB, readDataCallback, onComplete)
    }

    private fun readDataFromDb1(
        query: Query,
        readDataCallback: ReadDataCallback?,
        onComplete: (() -> Unit?)?,
    ) {
        query
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val adArray = ArrayList<Announcement>()
                    Log.d("DbManager", "Результаты запроса: ${task.result}")
                    if (!task.result!!.isEmpty) {
                        Log.d(
                            "DbManager",
                            "Количество документов в результате: ${task.result!!.size()}",
                        )
                        for (document in task.result!!) {
                            Log.d("DbManager", "Данные документа: ${document.data}")
                        }
                    } else {
                        Log.d("DbManager", "Результат запроса пуст.")
                    }

                    val lastDocument = task.result!!.lastOrNull()

                    for (document in task.result!!) {
                        val adData = document.data as Map<*, *>?
                        val ad =
                            adData?.let {
                                document.toObject(Announcement::class.java)
                            }
                        Log.d("DbManager", "adData: $adData")
                        Log.d("DbManager", "ad: $ad")

                        val favUids = adData?.get("favUids") as? List<String>

                        val isFav =
                            auth.uid?.let {
                                val containsUid = favUids?.contains(it) == true
                                val result = adData?.containsKey("favUids") == true && containsUid
                                result
                            }

                        if (isFav != null) {
                            ad?.isFav = isFav
                        }

                        val favCounter = favUids?.size
                        ad?.favCounter = favCounter.toString()
                        ad?.let { adArray.add(it) }
                    }

                    readDataCallback?.readData(adArray, lastDocument)
                    if (onComplete != null) {
                        onComplete()
                    }
                } else {
                    Log.e("DbManager", "Ошибка при получении данных: ${task.exception}")
                    // Обработка ошибки
                    if (onComplete != null) {
                        onComplete()
                    }
                }
            }
    }*/

    interface ReadDataCallback {
        fun readData(
            list: ArrayList<Announcement>,
            lastDocument: QueryDocumentSnapshot?,
        )

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
