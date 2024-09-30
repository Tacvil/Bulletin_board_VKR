package com.example.bulletin_board.packroom

import androidx.compose.ui.geometry.isEmpty
import com.example.bulletin_board.model.Ad
import com.example.bulletin_board.model.DbManager.Companion.USER_NODE
import com.example.bulletin_board.model.FavData
import com.example.bulletin_board.model.ViewData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.getField
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
            const val MAIN_COLLECTION = "main"
            const val FAV_UIDS_FIELD = "favUids"
            const val VIEWS_COUNTER_FIELD = "viewsCounter"
            const val TIME_FIELD = "time"
            const val UID_FIELD = "uid"
            const val IS_PUBLISHED_FIELD = "published"
            const val KEYWORDS_FIELD = "keyWords"
            const val COUNTRY_FIELD = "country"
            const val CITY_FIELD = "city"
            const val INDEX_FIELD = "index"
            const val CATEGORY_FIELD = "category"
            const val WITH_SEND_FIELD = "withSend"
            const val PRICE_FIELD = "price"
            const val PRICE_FROM_FIELD = "price_from"
            const val PRICE_TO_FIELD = "price_to"
            const val ORDER_BY_FIELD = "orderBy"
            const val TITLE_FIELD = "title"
            const val KEY_FIELD = "key"
            const val ADS_LIMIT = 2
        }

        /**
         * Вставляет новое объявление в Firestore.
         *
         * @param ad Объявление для вставки.
         */
        suspend fun insertAd(ad: Ad): Result<Boolean> =
            try {
                firestore
                    .collection(MAIN_COLLECTION)
                    .document(ad.key)
                    .set(ad)
                    .await()
                Result.Success(true)
            } catch (e: Exception) {
                Timber.e(e, "Error insert announcement")
                Result.Error(e)
            }

        /**
         * Удаляет объявление из Firestore.
         *
         * @param ad Объявление для удаления.
         * @return Result.Success(true) если объявление было успешно удалено,
         *         Result.Error(exception) в случае ошибки.
         */
        suspend fun deleteAd(adKey: String): Result<Boolean> =
            try {
                firestore
                    .collection(MAIN_COLLECTION)
                    .document(adKey)
                    .delete()
                    .await()
                Result.Success(true)
            } catch (e: Exception) {
                Timber.e(e, "Error deleting ad: $adKey")
                Result.Error(e)
            }

        /**
         * Увеличивает счетчик просмотров объявления на 1.
         *
         * @param ad Объявление, счетчик просмотров которого нужно увеличить.
         */
        suspend fun adViewed(viewData: ViewData): Result<ViewData> =
            try {
                val viewsCounter = viewData.viewsCounter + 1
                firestore
                    .collection(MAIN_COLLECTION)
                    .document(viewData.key)
                    .update(VIEWS_COUNTER_FIELD, viewsCounter)
                    .await()
                val updatedViewData = viewData.copy(viewsCounter = viewsCounter)
                Result.Success(updatedViewData)
            } catch (e: Exception) {
                Timber.e(e, "Error updating views counter for ad: ${viewData.key}")
                Result.Error(e)
            }

        /**
         * Обрабатывает нажатие на кнопку "Избранное" для объявления.
         *
         * @param ad Объявление, для которого нужно обработать нажатие.
         * @return Result.Success(true) если операция была успешной,
         *         Result.Error(exception) в случае ошибки.
         */
        suspend fun onFavClick(favData: FavData): Result<FavData> =
            try {
                auth.uid?.let { uid ->
                    firestore
                        .collection(MAIN_COLLECTION)
                        .document(favData.key)
                        .update(
                            FAV_UIDS_FIELD,
                            if (!favData.isFav) {
                                FieldValue.arrayUnion(uid)
                            } else {
                                FieldValue.arrayRemove(
                                    uid,
                                )
                            },
                        ).await()

                    val favCounter =
                        if (!favData.isFav) favData.favCounter.toInt() + 1 else favData.favCounter.toInt() - 1
                    val updatedAd =
                        FavData(
                            key = favData.key,
                            favCounter = favCounter.toString(),
                            isFav = !favData.isFav,
                        )
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
                Timber.d("getMyAds: uid = ${auth.uid}")
                var query =
                    firestore
                        .collection(MAIN_COLLECTION)
                        .whereEqualTo(UID_FIELD, auth.uid)
                        .orderBy(TIME_FIELD, Query.Direction.DESCENDING)
                        .limit(limit)

                if (key != null) {
                    query = query.startAfter(key.get(TIME_FIELD))
                }

                val snapshot = query.get().await()

                val ads = processAds(snapshot.documents.map { it.toObject(Ad::class.java)!! })

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

        suspend fun getMinPrice(category: String?): Result<Int> =
            try {
                val collectionReference = firestore.collection(MAIN_COLLECTION)
                val query =
                    if (!category.isNullOrEmpty()) {
                        collectionReference
                            .whereEqualTo(CATEGORY_FIELD, category)
                            .orderBy(PRICE_FIELD, Query.Direction.ASCENDING)
                            .limit(1)
                    } else {
                        collectionReference
                            .orderBy(PRICE_FIELD, Query.Direction.ASCENDING)
                            .limit(1)
                    }

                val snapshot = query.get().await()
                if (snapshot.isEmpty) {
                    Result.Error(Exception("Collection is empty"))
                } else {
                    val minPrice =
                        snapshot.documents[0].getField<Int?>(PRICE_FIELD)?.toInt()
                            ?: throw Exception("Price is null")
                    Result.Success(minPrice)
                }
            } catch (e: Exception) {
                Result.Error(e)
            }

        suspend fun getMaxPrice(category: String?): Result<Int> =
            try {
                val collectionReference = firestore.collection(MAIN_COLLECTION)
                val query =
                    if (!category.isNullOrEmpty()) {
                        collectionReference
                            .whereEqualTo(CATEGORY_FIELD, category)
                            .orderBy(PRICE_FIELD, Query.Direction.DESCENDING)
                            .limit(1)
                    } else {
                        collectionReference
                            .orderBy(PRICE_FIELD, Query.Direction.DESCENDING)
                            .limit(1)
                    }

                val snapshot = query.get().await()
                if (snapshot.isEmpty) {
                    Result.Error(Exception("Collection is empty"))
                } else {
                    val maxPrice =
                        snapshot.documents[0].getField<Int?>(PRICE_FIELD)?.toInt()
                            ?: throw Exception("Price is null")
                    Result.Success(maxPrice)
                }
            } catch (e: Exception) {
                Result.Error(e)
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

        suspend fun fetchSearchResults(inputSearchQuery: String): Result<List<String>> =
            try {
                val query =
                    firestore
                        .collection(MAIN_COLLECTION)
                        .whereGreaterThanOrEqualTo(TITLE_FIELD, inputSearchQuery)
                val documents = query.get().await()

                val results =
                    documents.documents.map { document ->
                        document.getString(TITLE_FIELD) ?: ""
                    }

                Result.Success(results)
            } catch (e: Exception) {
                Result.Error(e)
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
    }
