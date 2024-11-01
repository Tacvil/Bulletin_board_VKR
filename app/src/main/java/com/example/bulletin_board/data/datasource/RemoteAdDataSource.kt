package com.example.bulletin_board.data.datasource

import android.net.Uri
import com.example.bulletin_board.data.utils.Result
import com.example.bulletin_board.data.utils.SortOption
import com.example.bulletin_board.domain.model.Ad
import com.example.bulletin_board.domain.model.FavData
import com.example.bulletin_board.domain.model.ViewData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.getField
import com.google.firebase.storage.StorageReference
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class RemoteAdDataSource
    @Inject
    constructor(
        private val firestore: FirebaseFirestore,
        private val auth: FirebaseAuth,
        private val dbStorage: StorageReference,
    ) {
        companion object {
            const val MAIN_COLLECTION = "main"
            const val USERS_COLLECTION = "users"
            const val TOKEN_FIELD = "token"
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
            const val TITLE_LOWERCASE_FIELD = "titleLowercase"
            const val KEY_FIELD = "key"
            const val ADS_LIMIT = 2
        }

        /**
         * Inserts a new ad into the Firestore database.
         * @param ad The ad object to be inserted.
         * @return A Result object indicating success or failure.
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
                Result.Error(e)
            }

        /**
         * Deletes an ad from the Firestore database.
         * @param adKey The key of the ad to be deleted.
         * @return A Result object indicating success or failure.
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
         * Increments the views counter for an ad in the Firestore database.
         * @param viewData The ViewData object containing the ad key and current views counter.
         * @return A Result object containing the updated ViewData or an error.
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
                Result.Error(e)
            }

        /**
         * Handles the favorite click action for an ad.
         * Adds or removes the current user's UID from the favUids array in the Firestore database.
         * Updates the favCounter accordingly.
         * @param favData The FavData object containing the ad key, favCounter, and isFav status.
         * @return A Result object containing the updated FavData or an error.
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
                Result.Error(e)
            }

        /**
         * Retrieves the user's ads from the Firestore database.
         * @param key The document snapshot to start querying from (for pagination).
         * @param limit The maximum number of ads to retrieve.
         * @return A Pair containing the list of ads and the next document snapshot (for pagination).
         */
        suspend fun getMyAds(
            key: DocumentSnapshot? = null,
            limit: Long = ADS_LIMIT.toLong(),
        ): Pair<List<Ad>, DocumentSnapshot?> =
            try {
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
         * Retrieves the user's favorite ads from the Firestore database.
         * @param limit The maximum number of ads to retrieve.
         * @param startAfter The document snapshot to start querying from (for pagination).
         * @return A Pair containing the list of ads and the next document snapshot (for pagination).
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
         * Retrieves the minimum and maximum price for ads in a specific category.
         * @param category The category to filter by (optional).
         * @return A Result object containing a Pair of the minimum and maximum price, or an error.
         */
        suspend fun getMinMaxPrice(category: String?): Result<Pair<Int?, Int?>> =
            try {
                val collectionReference = firestore.collection(MAIN_COLLECTION)
                val query =
                    if (!category.isNullOrEmpty()) {
                        collectionReference.whereEqualTo(CATEGORY_FIELD, category)
                    } else {
                        collectionReference
                    }

                val minSnapshot =
                    query
                        .orderBy(PRICE_FIELD, Query.Direction.ASCENDING)
                        .limit(1)
                        .get()
                        .await()
                val maxSnapshot =
                    query
                        .orderBy(PRICE_FIELD, Query.Direction.DESCENDING)
                        .limit(1)
                        .get()
                        .await()

                val minPrice =
                    if (minSnapshot.isEmpty) {
                        null
                    } else {
                        minSnapshot.documents[0]
                            .getField<Int?>(
                                PRICE_FIELD,
                            )?.toInt()
                    }
                val maxPrice =
                    if (maxSnapshot.isEmpty) {
                        null
                    } else {
                        maxSnapshot.documents[0]
                            .getField<Int?>(
                                PRICE_FIELD,
                            )?.toInt()
                    }

                Result.Success(minPrice to maxPrice)
            } catch (e: Exception) {
                Result.Error(e)
            }

        /**
         * Retrieves all ads from the Firestore database, applying filters and pagination.
         * @param filter A map of filters to apply to the query.
         * @param key The document snapshot to start querying from (for pagination).
         * @param limit The maximum number of ads to retrieve.
         * @return A Pair containing the list of ads and the next document snapshot (for pagination).
         */
        suspend fun getAllAds(
            filter: MutableMap<String, String>,
            key: DocumentSnapshot? = null,
            limit: Long = ADS_LIMIT.toLong(),
        ): Pair<List<Ad>, DocumentSnapshot?> =
            try {
                val query = buildAdsQuery(filter, key)
                val snapshot = query.limit(limit).get().await()
                val ads =
                    snapshot.documents.map { it.toObject(Ad::class.java)!! }
                val processedAds = processAds(ads)

                val nextKey = snapshot.documents.lastOrNull()
                Pair(processedAds, nextKey)
            } catch (e: FirebaseFirestoreException) {
                Timber.e(e, "Error getting ads")
                Pair(emptyList(), null)
            }

        private fun processAds(ads: List<Ad>): List<Ad> =
            ads.map { ad ->
                ad.apply {
                    val favUids = this.favUids
                    isFav = favUids.contains(auth.uid)
                    favCounter = favUids.size.toString()
                }
            }

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

        private fun sortByKeyWords(
            query: Query,
            filter: MutableMap<String, String>,
        ): Query =
            if (filter[KEYWORDS_FIELD]?.isNotEmpty() == true) {
                query.whereArrayContains(KEYWORDS_FIELD, filter[KEYWORDS_FIELD]!!)
            } else {
                query
            }

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

        private fun sortByCategory(
            query: Query,
            filter: MutableMap<String, String>,
        ): Query =
            if (filter[CATEGORY_FIELD].isNullOrEmpty()) {
                query
            } else {
                query.whereEqualTo(CATEGORY_FIELD, filter[CATEGORY_FIELD])
            }

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
                null, "" -> query
                else -> query
            }

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
         * Fetches search results from the Firestore database based on the input query.
         * @param inputSearchQuery The search query entered by the user.
         * @return A Result object containing the list of search results or an error.
         */
        suspend fun fetchSearchResults(inputSearchQuery: String): Result<List<String>> =
            try {
                val query =
                    firestore
                        .collection(MAIN_COLLECTION)
                        .whereGreaterThanOrEqualTo(TITLE_LOWERCASE_FIELD, inputSearchQuery)
                val documents = query.get().await()

                val results =
                    documents.documents.map { document ->
                        document.getString(TITLE_FIELD) ?: ""
                    }

                Result.Success(results)
            } catch (e: Exception) {
                Result.Error(e)
            }

        /**
         * Uploads an image to Firebase Storage.
         * @param byteArray The byte array representation of the image.
         * @return A Result object containing the download URI of the uploaded image or an error.
         */
        suspend fun uploadImage(byteArray: ByteArray): Result<Uri> =
            try {
                val imageFileName = "image_${System.currentTimeMillis()}"
                val imageRef = dbStorage.child(auth.uid!!).child(imageFileName)
                val downloadUri =
                    imageRef
                        .putBytes(byteArray)
                        .continueWithTask { task ->
                            if (!task.isSuccessful) {
                                task.exception?.let { throw it }
                            }
                            imageRef.downloadUrl
                        }.await()
                Result.Success(downloadUri)
            } catch (e: Exception) {
                Result.Error(e)
            }

        /**
         * Updates an existing image in Firebase Storage.
         * @param byteArray The byte array representation of the new image.
         * @param url The URL of the existing image to be updated.
         * @return A Result object containing the download URI of the updated image or an error.
         */
        suspend fun updateImage(
            byteArray: ByteArray,
            url: String,
        ): Result<Uri> =
            try {
                val imageRef = dbStorage.storage.getReferenceFromUrl(url)
                val downloadUri =
                    imageRef
                        .putBytes(byteArray)
                        .continueWithTask { task ->
                            if (!task.isSuccessful) {
                                task.exception?.let { throw it }
                            }
                            imageRef.downloadUrl
                        }.await()
                Result.Success(downloadUri)
            } catch (e: Exception) {
                Result.Error(e)
            }

        /**
         * Deletes an image from Firebase Storage by its URL.
         * @param oldUrl The URL of the image to be deleted.
         * @return A Result object indicating success or failure.
         */
        suspend fun deleteImageByUrl(oldUrl: String) =
            try {
                val imageRef = dbStorage.storage.getReferenceFromUrl(oldUrl)
                imageRef.delete().await()
                Result.Success(true)
            } catch (e: Exception) {
                Result.Error(e)
            }

        /**
         * Saves the user's FCM token to the Firestore database.
         * @param token The FCM token to be saved.
         * @return A Result object indicating success or failure.
         */
        suspend fun saveToken(token: String): Result<Boolean> =
            try {
                val userRef = firestore.collection(USERS_COLLECTION).document(auth.uid ?: "")
                userRef.update(TOKEN_FIELD, token).await()
                Result.Success(true)
            } catch (e: Exception) {
                Result.Error(e)
            }
    }
