package com.example.bulletin_board.packroom

import com.example.bulletin_board.model.Ad
import com.example.bulletin_board.model.DbManager.Companion.USER_NODE
import com.example.bulletin_board.model.FavClickData
import com.example.bulletin_board.model.FavData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class RemoteAdDataSource
    @Inject
    constructor(
        private val firestore: FirebaseFirestore,
        private val auth: FirebaseAuth = Firebase.auth,
    ) {
        companion object {
            private const val MAIN_COLLECTION = "main"
            private const val FAV_UIDS_FIELD = "favUids"
            private const val VIEWS_COUNTER_FIELD = "viewsCounter"
            private const val TIME_FIELD = "time"
            private const val UID_FIELD = "uid"
            private const val IS_PUBLISHED_FIELD = "published"
            private const val KEYWORDS_FIELD = "keyWords"
            private const val COUNTRY_FIELD = "country"
            private const val CITY_FIELD = "city"
            private const val INDEX_FIELD = "index"
            private const val CATEGORY_FIELD = "category"
            private const val WITH_SEND_FIELD = "withSend"
            private const val PRICE_FIELD = "price"
            private const val PRICE_FROM_FIELD = "price_from"
            private const val PRICE_TO_FIELD = "price_to"
            private const val ORDER_BY_FIELD = "orderBy"
            private const val KEY_FIELD = "key"
            const val ADS_LIMIT = 2
        }

        /**
         * Вставляет новое объявление в Firestore.
         *
         * @param ad Объявление для вставки.
         */
        suspend fun insertAd(ad: Ad) {
            try {
                firestore
                    .collection(MAIN_COLLECTION)
                    .document(ad.key)
                    .set(ad)
                    .await()
            } catch (e: Exception) {
                Timber.e(e, "Error inserting ad")
            }
        }

        /**
         * Удаляет объявление из Firestore.
         *
         * @param ad Объявление для удаления.
         * @return Result.Success(true) если объявление было успешно удалено,
         *         Result.Error(exception) в случае ошибки.
         */
        suspend fun deleteAd(ad: Ad): Result<Boolean> =
            try {
                firestore
                    .collection(MAIN_COLLECTION)
                    .document(ad.key)
                    .delete()
                    .await()
                Result.Success(true)
            } catch (e: Exception) {
                Timber.e(e, "Error deleting ad: ${ad.key}")
                Result.Error(e)
            }

        /**
         * Увеличивает счетчик просмотров объявления на 1.
         *
         * @param ad Объявление, счетчик просмотров которого нужно увеличить.
         */
        suspend fun adViewed(ad: Ad) {
            try {
                val viewsCounter = ad.viewsCounter + 1
                firestore
                    .collection(MAIN_COLLECTION)
                    .document(ad.key)
                    .update(VIEWS_COUNTER_FIELD, viewsCounter)
                    .await()
            } catch (e: Exception) {
                Timber.e(e, "Error updating views counter for ad: ${ad.key}")
            }
        }

        /**
         * Обрабатывает нажатие на кнопку "Избранное" для объявления.
         *
         * @param ad Объявление, для которого нужно обработать нажатие.
         * @return Result.Success(true) если операция была успешной,
         *         Result.Error(exception) в случае ошибки.
         */
        suspend fun onFavClick(favClickData: FavClickData): Result<FavData?> =
            try {
                auth.uid?.let { uid ->
                    val update =
                        if (favClickData.isFav) {
                            FieldValue.arrayRemove(uid)
                        } else {
                            FieldValue.arrayUnion(uid)
                        }
                    firestore
                        .collection(MAIN_COLLECTION)
                        .document(favClickData.key)
                        .update(FAV_UIDS_FIELD, update)
                        .await()

                    // Получаем обновленное объявление
                    val updatedAd =
                        firestore
                            .collection(MAIN_COLLECTION)
                            .document(favClickData.key)
                            .get()
                            .await()
                            .toObject(FavData::class.java)

                    // Пересчитываем isFav и favCounter
                    updatedAd?.let {
                        it.isFav = it.favUids.contains(auth.uid)
                        it.favCounter = it.favUids.size.toString()
                    }

                    Result.Success(updatedAd)
                } ?: Result.Error(Exception("User not authenticated"))
            } catch (e: Exception) {
                Timber.e(e, "Error updating favorites")
                Result.Error(e)
            }

        /**
         * Возвращает Flow со списком объявлений текущего пользователя.
         *
         * @return Flow с Result.Success(list), содержащим список объявлений,
         *         или Result.Error(exception) в случае ошибки.
         */

        suspend fun getMyAds(
            key: DocumentSnapshot? = null,
            limit: Long = ADS_LIMIT.toLong(),
        ): Pair<List<Ad>, DocumentSnapshot?> =
            try {
                val query =
                    firestore
                        .collection(MAIN_COLLECTION)
                        .whereEqualTo(UID_FIELD, auth.uid)
                        .orderBy(TIME_FIELD, Query.Direction.DESCENDING)

                if (key != null) {
                    query.startAfter(key)
                }

                val snapshot = query.limit(limit).get().await()
                val ads =
                    snapshot.documents.map { ad ->
                        val adObj = ad.toObject(Ad::class.java)!!
                        val favUids = adObj.favUids
                        adObj.isFav = auth.uid?.let { favUids.contains(it) } ?: false
                        adObj.favCounter = favUids.size.toString()
                        adObj
                    }

                val nextKey = snapshot.documents.lastOrNull()
                Pair(ads, nextKey)
            } catch (e: Exception) {
                Timber.e(e, "Error getting my ads")
                Pair(emptyList(), null)
            }

        /**
         * Возвращает Flow со списком объявлений, добавленных в избранное текущим пользователем.
         *
         * @return Flow с Result.Success(list), содержащим список объявлений,
         *         или Result.Error(exception) в случае ошибки.
         */
        suspend fun getMyFavs(
            limit: Long = ADS_LIMIT.toLong(),
            startAfter: DocumentSnapshot? = null,
        ): Pair<List<Ad>, DocumentSnapshot?> =
            try {
                auth.uid?.let { uid ->
                    var query =
                        firestore
                            .collection(MAIN_COLLECTION)
                            .whereArrayContains(FAV_UIDS_FIELD, uid)
                            .orderBy(TIME_FIELD, Query.Direction.DESCENDING)
                            .limit(limit)

                    if (startAfter != null) {
                        query = query.startAfter(startAfter.get(TIME_FIELD))
                    }

                    val snapshot = query.get().await()
                    val ads =
                        snapshot.documents.map { document ->
                            document.toObject(Ad::class.java)!!.apply {
                                val favUids = this.favUids
                                isFav = favUids.contains(uid)
                                favCounter = favUids.size.toString()
                            }
                        }

                    val nextKey = snapshot.documents.lastOrNull()
                    Pair(ads, nextKey)
                } ?: Pair(emptyList(), null)
            } catch (e: Exception) {
                Timber.e(e, "Error getting my favorite ads")
                Pair(emptyList(), null)
            }

        /**
         * Получает все объявления из Firestore с учетом фильтров и пагинации.
         *
         * @param filter Фильтры для запроса.
         */
        suspend fun getAllAds(
            filter: MutableMap<String, String>,
            key: DocumentSnapshot? = null,
            limit: Long = ADS_LIMIT.toLong(),
        ): Pair<List<Ad>, DocumentSnapshot?> =
            try {
                val query = buildAdsQuery(filter, key)
                Timber.d("Query: $query")
                Timber.d("Query filter: $filter")
                Timber.d("Query key: $key")

                val snapshot = query.limit(limit).get().await()
                Timber.d("Query snapshot: $snapshot")
                val ads =
                    snapshot.documents.map { it.toObject(Ad::class.java)!! } // Преобразование в List<Ad>
                val processedAds = processAds(ads) // Обрабатываем список объявлений

                Timber.d("Query ads: $ads")
                ads.forEach { ad ->
                    Timber.d("Ad loaded getAllAds: $ad")
                }

                val nextKey = snapshot.documents.lastOrNull()
                Pair(processedAds, nextKey)
            } catch (e: FirebaseFirestoreException) {
                Timber.e(e, "Error getting ads")
                Pair(emptyList(), null) // Возвращаем пустой список и null в случае ошибки
            }

        private fun processAds(ads: List<Ad>): List<Ad> =
            ads.map { ad ->
                ad.apply {
                    val favUids = this.favUids
                    isFav = favUids.contains(auth.uid)
                    favCounter = favUids.size.toString()
                }
            }

        /**
         * Создает запрос Firestore для получения объявлений с учетом фильтров.
         *
         * @param context Контекст приложения.
         * @param filter Фильтры для запроса.
         * @return Запрос Firestore.
         */
        private fun buildAdsQuery(
            filter: MutableMap<String, String>,
            key: DocumentSnapshot? = null,
        ): Query {
            var query = firestore.collection(MAIN_COLLECTION).whereEqualTo(IS_PUBLISHED_FIELD, true)
            query = sortByKeyWords(query, filter)
            query = sortByLocation(query, filter)
            query = sortByCategory(query, filter)
            query = sortByWithSend(query, filter)
            query = sortByPrice(query, filter)
            query = sortByOrder(query, filter)
            query = applyPagination(query, filter, key)
            return query
        }

        /**
         * Сортирует запрос по ключевым словам.
         *
         * @param query Исходный запрос.
         * @param filter Фильтры для запроса.
         * @return Отсортированный запрос.
         */
        private fun sortByKeyWords(
            query: Query,
            filter: MutableMap<String, String>,
        ): Query =
            if (filter[KEYWORDS_FIELD]?.isNotEmpty() == true) {
                query.whereArrayContains(KEYWORDS_FIELD, filter[KEYWORDS_FIELD]!!)
            } else {
                query
            }

        /**
         * Сортирует запрос по местоположению (страна, город, индекс).
         *
         * @param initialQuery Исходный запрос.
         * @param filter Фильтры для запроса.
         * @return Отсортированный запрос.
         */
        private fun sortByLocation(
            initialQuery: Query,
            filter: MutableMap<String, String>,
        ): Query {
            var query = initialQuery
            if (filter[COUNTRY_FIELD]?.isNotEmpty() == true) {
                query = query.whereEqualTo(COUNTRY_FIELD, filter[COUNTRY_FIELD])
            }
            if (filter[CITY_FIELD]?.isNotEmpty() == true) {
                query = query.whereEqualTo(CITY_FIELD, filter[CITY_FIELD])
            }
            if (filter[INDEX_FIELD]?.isNotEmpty() == true) {
                query = query.whereEqualTo(INDEX_FIELD, filter[INDEX_FIELD])
            }
            return query
        }

        /**
         * Сортирует запрос по категории.
         *
         * @param query Исходный запрос.
         * @param filter Фильтры для запроса.
         * @return Отсортированный запрос.
         */
        private fun sortByCategory(
            query: Query,
            filter: MutableMap<String, String>,
        ): Query =
            if (filter[CATEGORY_FIELD].isNullOrEmpty()) {
                query
            } else {
                query.whereEqualTo(CATEGORY_FIELD, filter[CATEGORY_FIELD])
            }

        /**
         * Сортирует запрос по наличию отправки.
         *
         * @param query Исходный запрос.
         * @param filter Фильтры для запроса.
         * @return Отсортированный запрос.
         */
        private fun sortByWithSend(
            query: Query,
            filter: MutableMap<String, String>,
        ): Query =
            when (filter[WITH_SEND_FIELD]) {
                SortOption.WITH_SEND.id -> query.whereEqualTo(WITH_SEND_FIELD, SortOption.WITH_SEND.id)
                SortOption.WITHOUT_SEND.id ->
                    query.whereEqualTo(
                        WITH_SEND_FIELD,
                        SortOption.WITHOUT_SEND.id,
                    )

                else -> query
            }

        /**
         * Сортирует запрос по цене.
         *
         * @param query Исходный запрос.
         * @param filter Фильтры для запроса.
         * @return Отсортированный запрос.
         */
        private fun sortByPrice(
            query: Query,
            filter: MutableMap<String, String>,
        ): Query =
            if (filter[PRICE_FROM_FIELD]?.isNotEmpty() == true || filter[PRICE_TO_FIELD]?.isNotEmpty() == true) {
                try {
                    val priceFrom = filter[PRICE_FROM_FIELD]?.toIntOrNull() ?: 0
                    val priceTo = filter[PRICE_TO_FIELD]?.toIntOrNull() ?: Int.MAX_VALUE
                    query
                        .whereGreaterThanOrEqualTo(PRICE_FIELD, priceFrom)
                        .whereLessThanOrEqualTo(PRICE_FIELD, priceTo)
                } catch (e: Exception) {
                    Timber.e(e, "Error converting price")
                    query
                }
            } else {
                query
            }

        /**
         * Сортирует запрос по порядку (дата, популярность, цена).
         *
         * @param initialQuery Исходный запрос.
         * @param filter Фильтры для запроса.
         * @return Отсортированный запрос.
         */
        private fun sortByOrder(
            initialQuery: Query,
            filter: MutableMap<String, String>,
        ): Query {
            var query = initialQuery
            query =
                when (filter[ORDER_BY_FIELD]) {
                    SortOption.BY_NEWEST.id -> {
                        Timber.d("byNewest")
                        query.orderBy(TIME_FIELD, Query.Direction.DESCENDING)
                    }

                    SortOption.BY_POPULARITY.id ->
                        query
                            .orderBy(VIEWS_COUNTER_FIELD, Query.Direction.DESCENDING)
                            .orderBy(KEY_FIELD, Query.Direction.DESCENDING)

                    SortOption.BY_PRICE_ASC.id ->
                        query
                            .orderBy(PRICE_FIELD, Query.Direction.ASCENDING)
                            .orderBy(KEY_FIELD, Query.Direction.ASCENDING)

                    SortOption.BY_PRICE_DESC.id ->
                        query
                            .orderBy(PRICE_FIELD, Query.Direction.DESCENDING)
                            .orderBy(KEY_FIELD, Query.Direction.DESCENDING)

                    else -> query.orderBy(TIME_FIELD, Query.Direction.DESCENDING)
                }
            return query
        }

        /**
         * Применяет пагинацию к запросу.
         *
         * @param initialQuery Исходный запрос.
         * @param filter Фильтры для запроса.
         * @param lastDocument Последний документ из предыдущей страницы.
         * @return Запрос с пагинацией.
         */
        private fun applyPagination(
            initialQuery: Query,
            filter: MutableMap<String, String>,
            key: DocumentSnapshot?,
        ): Query {
            var query = initialQuery
            if (key != null) {
                query =
                    when (filter[ORDER_BY_FIELD]) {
                        SortOption.BY_PRICE_ASC.id, SortOption.BY_PRICE_DESC.id -> {
                            val lastPrice = key.getLong(PRICE_FIELD)?.toInt()
                            val lastKey = key.get(KEY_FIELD) as? String
                            if (lastPrice != null && lastKey != null) {
                                query.startAfter(lastPrice, lastKey)
                            } else {
                                query
                            }
                        }

                        SortOption.BY_POPULARITY.id -> {
                            val lastViewsCounter = key.getLong(VIEWS_COUNTER_FIELD)?.toInt()
                            val lastKey = key.get(KEY_FIELD) as? String
                            if (lastViewsCounter != null && lastKey != null) {
                                query.startAfter(lastViewsCounter, lastKey)
                            } else {
                                query
                            }
                        }

                        SortOption.BY_NEWEST.id -> {
                            val lastTime = key.get(TIME_FIELD) as? String
                            if (lastTime != null) {
                                query.startAfter(lastTime)
                            } else {
                                query
                            }
                        }

                        else -> {
                            val lastTime = key.get(TIME_FIELD) as? String
                            if (lastTime != null) {
                                query.startAfter(lastTime)
                            } else {
                                query
                            }
                        }
                    }
            }
            return query
        }

        /**
         * Получает объявления из Firestore по заданному запросу.
         *
         * @param query Запрос Firestore.
         * @param firestoreAdsCallback Callback для обработки результатов запроса.
         */
        private fun getAdsFromFirestore(
            query: Query,
            firestoreAdsCallback: FirestoreAdsCallback?,
        ) {
            query.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val ads = ArrayList<Ad>()
                    val lastDocument = task.result!!.lastOrNull()

                    for (document in task.result!!) {
                        try {
                            val ad = document.toObject(Ad::class.java)
                            val favUids = document.get(FAV_UIDS_FIELD) as? List<String>

                            ad.isFav = auth.uid?.let { favUids?.contains(it) } ?: false
                            ad.favCounter = favUids?.size.toString()

                            ads.add(ad)
                        } catch (e: Exception) {
                            Timber.e(e, "Error converting ad data")
                        }
                    }

                    firestoreAdsCallback?.readData(ads, lastDocument)
                } else {
                    Timber.e(task.exception, "Error getting data")
                    firestoreAdsCallback?.onError(task.exception ?: Exception("Unknown error"))
                }
                firestoreAdsCallback?.onComplete()
            }
        }

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

        /**
         * Callback для обработки результатов запроса объявлений из Firestore.
         */
        interface FirestoreAdsCallback {
            /**
             * Вызывается при успешном получении данных.
             *
             * @param list Список полученных объявлений.
             * @param lastDocument Последний документ из результата запроса (для пагинации).
             */
            fun readData(
                list: ArrayList<Ad>,
                lastDocument: QueryDocumentSnapshot?,
            )

            /**
             * Вызывается после завершения запроса (как при успехе, так и при ошибке).
             */
            fun onComplete()

            /**
             * Вызывается при возникновении ошибки во время запроса.
             *
             * @param e Исключение, возникшее во время запроса.
             */
            fun onError(e: Exception)
        }
    }
