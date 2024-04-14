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

class DbManager {
    val database = Firebase.database.getReference(MAIN_NODE)
    private val firestore = FirebaseFirestore.getInstance()
    val dbStorage = Firebase.storage.getReference(MAIN_NODE)
    val auth = Firebase.auth

    fun saveToken(token: String){
        if (auth.uid != null) {
            val userRef = firestore.collection(USER_NODE).document(auth.uid ?: "empty")
            // Создаем HashMap с полем "token"
            val tokenData = hashMapOf(
                "token" to token
            )
            // Обновляем данные пользователя в Firestore, добавляя поле "token"
            userRef
                .set(tokenData, SetOptions.merge()) // Используем SetOptions.merge(), чтобы добавить поле без удаления существующих данных
                .addOnSuccessListener {
                    println("Токен успешно сохранен для пользователя с ID: ${auth.uid}")
                }
                .addOnFailureListener { e ->
                    println("Ошибка при сохранении токена для пользователя с ID: ${auth.uid}, ошибка: $e")
                }
        }
    }

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

        Log.d("DBCounter", "Counter = $counter")

        val dataToUpdate = hashMapOf<String, Any>(
            "viewsCounter" to counter.toString()
        )

        if (auth.uid != null) firestore.collection(MAIN_NODE).document(ad.key ?: "empty")
            .update(dataToUpdate)
    }

    fun onFavClick(ad: Announcement, listener: FinishWorkListener) {
        if (ad.isFav) removeFromFavs(ad, listener) else addToFavs(ad, listener)
    }

    private fun addToFavs(ad: Announcement, listener: FinishWorkListener) {
        ad.key?.let {
            auth.uid?.let { uid ->
                firestore.collection(MAIN_NODE).document(it).update("favUids", FieldValue.arrayUnion(uid)).addOnCompleteListener {
                    if (it.isSuccessful) listener.onFinish(true)
                }

/*                database.child(it).child(FAVS_NODE)
                    .child(uid).setValue(uid).addOnCompleteListener {
                        if (it.isSuccessful) listener.onFinish(true)
                    }*/
            }
        }
    }

    private fun removeFromFavs(ad: Announcement, listener: FinishWorkListener) {
        ad.key?.let {
            auth.uid?.let { uid ->
                firestore.collection(MAIN_NODE).document(it).update("favUids", FieldValue.arrayRemove(uid)).addOnCompleteListener {
                    if (it.isSuccessful) listener.onFinish(true)
                }

/*                database.child(it).child(FAVS_NODE)
                    .child(uid).removeValue().addOnCompleteListener {
                        if (it.isSuccessful) listener.onFinish(true)
                    }*/
            }
        }
    }

        fun getMyAnnouncement(readDataCallback: ReadDataCallback?) {
            val query = firestore.collection(MAIN_NODE).whereEqualTo("uid", auth.uid).orderBy("time", Query.Direction.DESCENDING)
            readDataFromDb1(query, readDataCallback)
        }

        fun getMyFavs(readDataCallback: ReadDataCallback?) {
            val query =
                auth.uid?.let { firestore.collection(MAIN_NODE).whereArrayContains("favUids", it).orderBy("time", Query.Direction.DESCENDING) }
            if (query != null) {
                readDataFromDb1(query, readDataCallback)
            }
        }

    fun getAllAnnouncementFirstPage1(
        context: Context,
        filter: MutableMap<String, String>,
        readDataCallback: ReadDataCallback?
    ) {
        val query = if (filter.isEmpty()) {
            firestore.collection(MAIN_NODE).orderBy("time", Query.Direction.ASCENDING)
                .limit(ADS_LIMIT.toLong())
        } else {
            getAllAnnouncementByFilterFirstPage1(context,filter)
        }
        //val query = firestore.collection(MAIN_NODE).orderBy("time", Query.Direction.ASCENDING)

        readDataFromDb1(query, readDataCallback)
    }

    private fun getAllAnnouncementByFilterFirstPage1(
        context: Context,
        filter: MutableMap<String, String>
    ): Query {

        var queryDB: Query = firestore.collection(MAIN_NODE)

        //queryDB = queryDB.whereEqualTo("isPublished", true)

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
        when (filter["withSend"]?.isNotEmpty() == true){
            (filter["withSend"] == "Не важно") -> {}
            (filter["withSend"] == "С отправкой") -> {
                queryDB = queryDB.whereEqualTo("withSend", filter["withSend"])
            }
            (filter["withSend"] == "Без отправки") -> {
                queryDB = queryDB.whereEqualTo("withSend", filter["withSend"])
            }
            else -> {Log.d("DbManager_GAABFFP", "when -> else")}
        }
        if (filter["price_from"]?.isNotEmpty() == true || filter["price_to"]?.isNotEmpty() == true) {
            if (filter["price_from"]?.isNotEmpty() == true && filter["price_to"]?.isNotEmpty() == true){
                when (filter["orderBy"]?.isNotEmpty() == true){
                    (filter["orderBy"] == "По возрастанию цены") -> {
                        queryDB = queryDB
                            .whereGreaterThanOrEqualTo("price", filter["price_from"]?.toInt()!!)
                            .whereLessThanOrEqualTo("price", filter["price_to"]?.toInt()!!)
                            .orderBy("price", Query.Direction.ASCENDING)
                            .limit(ADS_LIMIT.toLong())
                    }
                    (filter["orderBy"] == "По убыванию цены") -> {
                        queryDB = queryDB
                            .whereGreaterThanOrEqualTo("price", filter["price_from"]?.toInt()!!)
                            .whereLessThanOrEqualTo("price", filter["price_to"]?.toInt()!!)
                            .orderBy("price", Query.Direction.DESCENDING)
                            .limit(ADS_LIMIT.toLong())
                    }
                    else -> {Log.d("DbManager_GAABFFP", "when orderBy PRICE1 -> else")}
                }
            }else {
                if (filter["price_from"]?.isNotEmpty() == true){
                    when (filter["orderBy"]?.isNotEmpty() == true){
                        (filter["orderBy"] == "По возрастанию цены") -> {
                            queryDB = queryDB
                                .whereGreaterThanOrEqualTo("price", filter["price_from"]?.toInt()!!)
                                .orderBy("price", Query.Direction.ASCENDING)
                                .limit(ADS_LIMIT.toLong())
                        }
                        (filter["orderBy"] == "По убыванию цены") -> {
                            queryDB = queryDB
                                .whereGreaterThanOrEqualTo("price", filter["price_from"]?.toInt()!!)
                                .orderBy("price", Query.Direction.DESCENDING)
                                .limit(ADS_LIMIT.toLong())
                        }
                        else -> {Log.d("DbManager_GAABFFP", "when orderBy PRICE2 -> else")}
                    }
                }else{
                    when (filter["orderBy"]?.isNotEmpty() == true){
                        (filter["orderBy"] == "По возрастанию цены") -> {
                            queryDB = queryDB
                                .whereLessThanOrEqualTo("price", filter["price_to"]?.toInt()!!)
                                .orderBy("price", Query.Direction.ASCENDING)
                                .limit(ADS_LIMIT.toLong())
                        }
                        (filter["orderBy"] == "По убыванию цены") -> {
                            queryDB = queryDB
                                .whereLessThanOrEqualTo("price", filter["price_to"]?.toInt()!!)
                                .orderBy("price", Query.Direction.DESCENDING)
                                .limit(ADS_LIMIT.toLong())
                        }
                        else -> {Log.d("DbManager_GAABFFP", "when orderBy PRICE3 -> else")}
                    }
                }
            }
        }else{
            when (filter["orderBy"]?.isNotEmpty() == true){
                (filter["orderBy"] == "По новинкам") -> {
                    queryDB = queryDB.orderBy("time", Query.Direction.DESCENDING)
                        .limit(ADS_LIMIT.toLong())
                }
                (filter["orderBy"] == "По популярности") -> {
                    queryDB = queryDB.orderBy("viewsCounter", Query.Direction.DESCENDING)
                        .limit(ADS_LIMIT.toLong())
                }
                (filter["orderBy"] == "По возрастанию цены") -> {
                    queryDB = queryDB.orderBy("price", Query.Direction.ASCENDING)
                        .limit(ADS_LIMIT.toLong())
                }
                (filter["orderBy"] == "По убыванию цены") -> {
                    queryDB = queryDB.orderBy("price", Query.Direction.DESCENDING)
                        .limit(ADS_LIMIT.toLong())
                }
                else -> {Log.d("DbManager_GAABFFP", "when orderBy1 -> else")}
            }
        }

        return queryDB
/*        return firestore.collection(MAIN_NODE).whereArrayContains("keyWords", value).orderBy("time", Query.Direction.ASCENDING).limit(
            ADS_LIMIT.toLong())*/
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
            context: Context,
            time: String,
            price: Int?,
            viewsCounter: String,
            lastDocumentAds: QueryDocumentSnapshot?,
            filter: MutableMap<String, String>,
            readDataCallback: ReadDataCallback?
        ) {
        if (filter.isEmpty()) {
            val query = firestore.collection(MAIN_NODE).whereGreaterThan("time", time).limit(
                ADS_LIMIT.toLong())
            readDataFromDb1(query, readDataCallback)
        } else {
            getAllAnnouncementByFilterNextPage1(context, filter, time, price, viewsCounter, lastDocumentAds, readDataCallback)
        }
    }

        private fun getAllAnnouncementByFilterNextPage1(
            context: Context,
            filter: MutableMap<String, String>,
            time: String,
            price: Int?,
            viewsCounter: String,
            lastDocumentAds: QueryDocumentSnapshot?,
            readDataCallback: ReadDataCallback?
        ) {
            var queryDB: Query = firestore.collection(MAIN_NODE)

            //queryDB = queryDB.whereEqualTo("isPublished", true)

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
            when (filter["withSend"]?.isNotEmpty() == true){
                (filter["withSend"] == "Не важно") -> {}
                (filter["withSend"] == "С отправкой") -> {
                    queryDB = queryDB.whereEqualTo("withSend", filter["withSend"])
                }
                (filter["withSend"] == "Без отправки") -> {
                    queryDB = queryDB.whereEqualTo("withSend", filter["withSend"])
                }
                else -> {Log.d("DbManager_GAABFFP", "when -> else")}
            }
            if (filter["price_from"]?.isNotEmpty() == true || filter["price_to"]?.isNotEmpty() == true) {
                if (filter["price_from"]?.isNotEmpty() == true && filter["price_to"]?.isNotEmpty() == true){
                    when (filter["orderBy"]?.isNotEmpty() == true){
                        (filter["orderBy"] == "По возрастанию цены") -> {
                            queryDB = queryDB.whereGreaterThanOrEqualTo("price", price!!)
                                .whereLessThanOrEqualTo("price", filter["price_to"]?.toInt()!!)
                                .orderBy("price", Query.Direction.ASCENDING)
                                .orderBy("key", Query.Direction.ASCENDING)
                                .startAfter(lastDocumentAds?.get("price") ?: "", lastDocumentAds?.get("key") ?: "")
                                .limit(ADS_LIMIT.toLong())
                        }
                        (filter["orderBy"] == "По убыванию цены") -> {
                            queryDB = queryDB.whereGreaterThanOrEqualTo("price", price!!)
                                .whereLessThanOrEqualTo("price", filter["price_to"]?.toInt()!!)
                                .orderBy("price", Query.Direction.DESCENDING)
                                .orderBy("key", Query.Direction.ASCENDING)
                                .startAfter(lastDocumentAds?.get("price") ?: "", lastDocumentAds?.get("key") ?: "")
                                .limit(ADS_LIMIT.toLong())
                        }
                        else -> {Log.d("DbManager_GAABFFP", "when orderBy PRICE4 -> else")}
                    }
                }else {
                    if (filter["price_from"]?.isNotEmpty() == true){
                        when (filter["orderBy"]?.isNotEmpty() == true){
                            (filter["orderBy"] == "По возрастанию цены") -> {
                                queryDB = queryDB.whereGreaterThanOrEqualTo("price", price!!)
                                    .orderBy("price", Query.Direction.ASCENDING)
                                    .orderBy("key", Query.Direction.ASCENDING)
                                    .startAfter(lastDocumentAds?.get("price") ?: "", lastDocumentAds?.get("key") ?: "")
                                    .limit(ADS_LIMIT.toLong())
                            }
                            (filter["orderBy"] == "По убыванию цены") -> {
                                queryDB = queryDB.whereGreaterThanOrEqualTo("price", price!!)
                                    .orderBy("price", Query.Direction.DESCENDING)
                                    .orderBy("key", Query.Direction.ASCENDING)
                                    .startAfter(lastDocumentAds?.get("price") ?: "", lastDocumentAds?.get("key") ?: "")
                                    .limit(ADS_LIMIT.toLong())
                            }
                            else -> {Log.d("DbManager_GAABFFP", "when orderBy PRICE5 -> else")}
                        }
                    }else{
                        when (filter["orderBy"]?.isNotEmpty() == true){
                            (filter["orderBy"] == "По возрастанию цены") -> {
                                queryDB = queryDB.whereGreaterThanOrEqualTo("price", price!!)
                                    .whereLessThanOrEqualTo("price", filter["price_to"]!!)
                                    .orderBy("price", Query.Direction.ASCENDING)
                                    .orderBy("key", Query.Direction.ASCENDING)
                                    .startAfter(lastDocumentAds?.get("price") ?: "", lastDocumentAds?.get("key") ?: "")
                                    .limit(ADS_LIMIT.toLong())
                            }
                            (filter["orderBy"] == "По убыванию цены") -> {
                                queryDB = queryDB.whereLessThanOrEqualTo("price", price!!)
                                    .orderBy("price", Query.Direction.DESCENDING)
                                    .orderBy("key", Query.Direction.ASCENDING)
                                    .startAfter(lastDocumentAds?.get("price") ?: "", lastDocumentAds?.get("key") ?: "")
                                    .limit(ADS_LIMIT.toLong())
                            }
                            else -> {Log.d("DbManager_GAABFFP", "when orderBy PRICE6 -> else")}
                        }
                    }
                }
            }else if(filter["orderBy"]?.isNotEmpty() == true){
                when {
                    (filter["orderBy"] == "По новинкам") -> {
                        queryDB = queryDB.whereLessThan("time", time).orderBy("time", Query.Direction.DESCENDING).limit(ADS_LIMIT.toLong())
                    }
                    (filter["orderBy"] == "По популярности") -> {
                        queryDB = queryDB.whereLessThanOrEqualTo("viewsCounter", viewsCounter)
                            .orderBy("viewsCounter", Query.Direction.DESCENDING)
                            .orderBy("key", Query.Direction.DESCENDING)
                            .startAfter(lastDocumentAds?.get("viewsCounter") ?: "", lastDocumentAds?.get("key") ?: "")
                            .limit(ADS_LIMIT.toLong())
                    }
                    (filter["orderBy"] == "По возрастанию цены") -> {
                        queryDB = queryDB.whereGreaterThanOrEqualTo("price", price!!)
                            .orderBy("price", Query.Direction.ASCENDING)
                            .orderBy("key", Query.Direction.ASCENDING)
                            .startAfter(lastDocumentAds?.get("price") ?: "", lastDocumentAds?.get("key") ?: "")
                            .limit(ADS_LIMIT.toLong())
                    }
                    (filter["orderBy"] == "По убыванию цены") -> {
                        queryDB = queryDB.whereLessThanOrEqualTo("price", price!!)
                            .orderBy("price", Query.Direction.DESCENDING)
                            .orderBy("key", Query.Direction.DESCENDING)
                            .startAfter(lastDocumentAds?.get("price") ?: "", lastDocumentAds?.get("key") ?: "")
                            .limit(ADS_LIMIT.toLong())
                    }
                    else -> {Log.d("DbManager_GAABFFP", "when orderBy21 -> else")}
                }
            }else {queryDB.whereLessThan("time", time).orderBy("time", Query.Direction.DESCENDING)}

/*            val query = firestore.collection(MAIN_NODE).whereArrayContains("keyWords", value).whereGreaterThan("time", time).limit(
                    ADS_LIMIT.toLong())*/

            readDataFromDb1(queryDB, readDataCallback)
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

                    val lastDocument = task.result!!.lastOrNull()

                    for (document in task.result!!) {
                        val adData = document.data as Map<*, *>?
                        val ad = adData?.let {
                            document.toObject(Announcement::class.java)
                        }
                        Log.d("DbManager", "adData: $adData")
                        Log.d("DbManager", "ad: $ad")


                        val favUids = adData?.get("favUids") as? List<String>

                        val isFav = auth.uid?.let {
                            val containsUid = favUids?.contains(it) == true
                            val result = adData?.containsKey("favUids") == true && containsUid
                            result
                        }

                        if (isFav != null) {
                            ad?.isFav = isFav
                        }

                        val favCounter = favUids?.size
                        ad?.favCounter = favCounter.toString()


                        //val infoItem = document.data["info"] as InfoItem?
                        /*                        val favCounter = document.reference.collection("favs").document().get().result

                                                ad?.isFav = ad?.uid?.let {
                                                    document.reference.collection("favs").document(it).get().isSuccessful
                                                } ?: false
                                                ad?.favCounter = favCounter?.toString() ?: "0"*/
/*                        ad?.viewsCounter = infoItem?.viewsCounter ?: "0"
                        ad?.emailCounter = infoItem?.emailCounter ?: "0"
                        ad?.callsCounter = infoItem?.callsCounter ?: "0"*/

                        ad?.let { adArray.add(it) }
                    }

                    readDataCallback?.readData(adArray, lastDocument)
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
        fun readData(list: ArrayList<Announcement>, lastDocument:  QueryDocumentSnapshot?)
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