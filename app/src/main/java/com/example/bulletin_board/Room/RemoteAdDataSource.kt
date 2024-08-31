package com.example.bulletin_board.Room

import android.content.Context
import androidx.lifecycle.get
import androidx.lifecycle.map
import com.example.bulletin_board.R
import com.example.bulletin_board.model.Ad
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.ktx.Firebase
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import kotlin.text.get
import kotlin.text.toLong

@Singleton
class RemoteAdDataSource
    @Inject
    constructor(
        private val firestore: FirebaseFirestore,
    ) : AdDataSource {
        private val auth = Firebase.auth

        companion object {
            private const val MAIN_COLLECTION = "main"
            private const val FAV_UIDS_FIELD = "favUids"
            private const val VIEWS_COUNTER_FIELD = "viewsCounter"
            private const val TIME_FIELD = "time"
            private const val UID_FIELD = "uid"
            private const val IS_PUBLISHED_FIELD = "isPublished"
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
        override suspend fun insertAd(ad: Ad) {
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
        override suspend fun deleteAd(ad: Ad): Result<Boolean> =
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
        suspend fun onFavClick(ad: Ad): Result<Boolean> =
            if (ad.isFav) {
                removeFromFavs(ad)
            } else {
                addToFavs(ad)
            }

        /**
         * Добавляет объявление в избранное текущего пользователя.
         *
         * @param ad Объявление для добавления в избранное.
         * @return Result.Success(true) если объявление было успешно добавлено,
         *         Result.Error(exception) в случае ошибки.
         */
        private suspend fun addToFavs(ad: Ad): Result<Boolean> =
            try {
                auth.uid?.let { uid ->
                    firestore
                        .collection(MAIN_COLLECTION)
                        .document(ad.key)
                        .update(FAV_UIDS_FIELD, FieldValue.arrayUnion(uid))
                        .await()
                    Result.Success(true)
                } ?: Result.Error(Exception("User not authenticated"))
            } catch (e: Exception) {
                Timber.e(e, "Error adding ad to favorites")
                Result.Error(e)
            }

        /**
         * Удаляет объявление из избранного текущего пользователя.
         *
         * @param ad Объявление для удаления из избранного.
         * @return Result.Success(true) если объявление было успешно удалено,
         *         Result.Error(exception) в случае ошибки.
         */
        private suspend fun removeFromFavs(ad: Ad): Result<Boolean> =
            try {
                auth.uid?.let { uid ->
                    firestore
                        .collection(MAIN_COLLECTION)
                        .document(ad.key)
                        .update(FAV_UIDS_FIELD, FieldValue.arrayRemove(uid))
                        .await()
                    Result.Success(true)
                } ?: Result.Error(Exception("User not authenticated"))
            } catch (e: Exception) {
                Timber.e(e, "Error removing ad from favorites")
                Result.Error(e)
            }

        /**
         * Возвращает Flow со списком объявлений текущего пользователя.
         *
         * @return Flow с Result.Success(list), содержащим список объявлений,
         *         или Result.Error(exception) в случае ошибки.
         */

        fun getMyAds(): Flow<Result<List<Ad>>> =
            flow {
                try {
                    val ads =
                        firestore
                            .collection(MAIN_COLLECTION)
                            .whereEqualTo(UID_FIELD, auth.uid)
                            .orderBy(TIME_FIELD, Query.Direction.DESCENDING)
                            .get()
                            .await()
                            .toObjects(Ad::class.java)
                            .map { ad ->
                                val favUids = ad.favUids
                                ad.isFav = auth.uid?.let { favUids.contains(it) } ?: false
                                ad.favCounter = favUids.size.toString()
                                ad
                            }
                    emit(Result.Success(ads))
                } catch (e: Exception) {
                    Timber.e(e, "Error getting my ads")
                    emit(Result.Error(e))
                }
            }

        /**
         * Возвращает Flow со списком объявлений, добавленных в избранное текущим пользователем.
         *
         * @return Flow с Result.Success(list), содержащим список объявлений,
         *         или Result.Error(exception) в случае ошибки.
         */
        fun getMyFavs(): Flow<Result<List<Ad>>> =
            flow {
                try {
                    auth.uid?.let { uid ->
                        val ads =
                            firestore
                                .collection(MAIN_COLLECTION)
                                .whereArrayContains(FAV_UIDS_FIELD, uid)
                                .orderBy(TIME_FIELD, Query.Direction.DESCENDING)
                                .get()
                                .await()
                                .toObjects(Ad::class.java)
                                .map { ad ->
                                    val favUids = ad.favUids
                                    ad.isFav = auth.uid?.let { favUids.contains(it) } ?: false
                                    ad.favCounter = favUids.size.toString()
                                    ad
                                }
                        emit(Result.Success(ads))
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error getting my favorite ads")
                    emit(Result.Error(e))
                }
            }

        /**
         * Получает все объявления из Firestore с учетом фильтров и пагинации.
         *
         * @param context Контекст приложения.
         * @param filter Фильтры для запроса.
         * @param time Время для пагинации (для сортировки по времени).
         * @param viewsCounter Счетчик просмотров для пагинации (для сортировки по популярности).
         * @param lastDocument Последний документ из предыдущей страницы (для пагинации).
         * @param firestoreAdsCallback Callback для обработки результатов запроса.
         */
        fun getAllAds(
            context: Context,
            filter: MutableMap<String, String>,
            time: String? = null,
            viewsCounter: Int? = null,
            lastDocument: QueryDocumentSnapshot? = null,
        ): Flow<Result<Pair<List<Ad>, QueryDocumentSnapshot?>>> =
            flow {
                try {
                    val query = buildAdsQuery(context, filter, time, viewsCounter, lastDocument)
                    val snapshot = query.get().await()
                    val ads =
                        snapshot.documents.map { document ->
                            val ad = document.toObject(Ad::class.java)!!
                            val favUids = document.get(FAV_UIDS_FIELD) as? List<String>
                            ad.isFav = auth.uid?.let { favUids?.contains(it) } ?: false
                            ad.favCounter = favUids?.size.toString()
                            ad
                        }
                    emit(Result.Success(Pair(ads, snapshot.documents.lastOrNull() as? QueryDocumentSnapshot)))
                } catch (e: Exception) {
                    Timber.e(e, "Error getting ads")
                    emit(Result.Error(e))
                }
            }

        /**
         * Создает запрос Firestore для получения объявлений с учетом фильтров.
         *
         * @param context Контекст приложения.
         * @param filter Фильтры для запроса.
         * @param time Время для пагинации (для сортировки по времени).
         * @param viewsCounter Счетчик просмотров для пагинации (для сортировки по популярности).
         * @param lastDocument Последний документ из предыдущей страницы (для пагинации).
         * @return Запрос Firestore.
         */
        private fun buildAdsQuery(
            context: Context,
            filter: MutableMap<String, String>,
            time: String? = null,
            viewsCounter: Int? = null,
            lastDocument: QueryDocumentSnapshot? = null,
        ): Query {
            var query = firestore.collection(MAIN_COLLECTION).whereEqualTo(IS_PUBLISHED_FIELD, true)
            query = sortByKeyWords(query, filter)
            query = sortByLocation(query, filter)
            query = sortByCategory(query, filter, context)
            query = sortByWithSend(query, filter)
            query = sortByPrice(query, filter)
            query = sortByOrder(query, filter, time, viewsCounter)
            query = applyPagination(query, filter, lastDocument)
            return query.limit(ADS_LIMIT.toLong())
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
         * @param context Контекст приложения.
         * @return Отсортированный запрос.
         */
        private fun sortByCategory(
            query: Query,
            filter: MutableMap<String, String>,
            context: Context,
        ): Query =
            if (!filter[CATEGORY_FIELD].isNullOrEmpty() &&
                filter[CATEGORY_FIELD] !=
                context.getString(
                    R.string.def,
                )
            ) {
                query.whereEqualTo(CATEGORY_FIELD, filter[CATEGORY_FIELD])
            } else {
                query
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
         * @param time Время для пагинации (для сортировки по времени).
         * @param viewsCounter Счетчик просмотров для пагинации (для сортировки по популярности).
         * @return Отсортированный запрос.
         */
        private fun sortByOrder(
            initialQuery: Query,
            filter: MutableMap<String, String>,
            time: String?,
            viewsCounter: Int?,
        ): Query {
            var query = initialQuery
            query =
                when (filter[ORDER_BY_FIELD]) {
                    SortOption.BY_NEWEST.id -> {
                        if (time != null) query.whereLessThan(TIME_FIELD, time)
                        query.orderBy(TIME_FIELD, Query.Direction.DESCENDING)
                    }

                    SortOption.BY_POPULARITY.id -> {
                        if (viewsCounter != null) {
                            query.whereLessThanOrEqualTo(
                                VIEWS_COUNTER_FIELD,
                                viewsCounter,
                            )
                        }
                        query
                            .orderBy(VIEWS_COUNTER_FIELD, Query.Direction.DESCENDING)
                            .orderBy(KEY_FIELD, Query.Direction.DESCENDING)
                    }

                    SortOption.BY_PRICE_ASC.id -> {
                        query
                            .orderBy(PRICE_FIELD, Query.Direction.ASCENDING)
                            .orderBy(KEY_FIELD, Query.Direction.ASCENDING)
                    }

                    SortOption.BY_PRICE_DESC.id -> {
                        query
                            .orderBy(PRICE_FIELD, Query.Direction.DESCENDING)
                            .orderBy(KEY_FIELD, Query.Direction.DESCENDING)
                    }

                    else -> {
                        if (time != null) query.whereLessThan(TIME_FIELD, time)
                        query.orderBy(TIME_FIELD, Query.Direction.DESCENDING)
                    }
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
            lastDocument: QueryDocumentSnapshot?,
        ): Query {
            var query = initialQuery
            if (lastDocument != null) {
                query =
                    when (filter[ORDER_BY_FIELD]) {
                        SortOption.BY_PRICE_ASC.id, SortOption.BY_PRICE_DESC.id -> {
                            val lastPrice = lastDocument.getLong(PRICE_FIELD)?.toInt()
                            val lastKey = lastDocument.get(KEY_FIELD) as? String
                            if (lastPrice != null && lastKey != null) {
                                query.startAfter(lastPrice, lastKey)
                            } else {
                                query
                            }
                        }

                        SortOption.BY_POPULARITY.id -> {
                            val lastViewsCounter = lastDocument.getLong(VIEWS_COUNTER_FIELD)?.toInt()
                            val lastKey = lastDocument.get(KEY_FIELD) as? String
                            if (lastViewsCounter != null && lastKey != null) {
                                query.startAfter(lastViewsCounter, lastKey)
                            } else {
                                query
                            }
                        }

                        SortOption.BY_NEWEST.id -> {
                            val lastTime = lastDocument.get(TIME_FIELD) as? String
                            if (lastTime != null) {
                                query.startAfter(lastTime)
                            } else {
                                query
                            }
                        }

                        else -> {
                            val lastTime = lastDocument.get(TIME_FIELD) as? String
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
