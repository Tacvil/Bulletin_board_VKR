package com.example.bulletin_board.adapterFirestore

import android.content.Context
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.bulletin_board.packroom.RemoteAdDataSource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import timber.log.Timber

class AdsPagingSource(
    private val remoteAdDataSource: RemoteAdDataSource,
    private val filter: MutableMap<String, String>,
    private val context: Context,
) : PagingSource<QueryDocumentSnapshot, DocumentSnapshot>() {
    override suspend fun load(params: LoadParams<QueryDocumentSnapshot>): LoadResult<QueryDocumentSnapshot, DocumentSnapshot> =
        try {
            Timber.d("Paging: Loading ads : getAllAds")
            val (ads, nextKey) = remoteAdDataSource.getAllAds(context, filter, params.key?.toString())

            // Выводим все данные из каждого DocumentSnapshot в лог
            ads.forEach { documentSnapshot ->
                documentSnapshot.data?.forEach { (key, value) ->
                    Timber.d("Firestore data: $key = $value")
                }
            }

            LoadResult.Page(ads, null, nextKey)
        } catch (e: Exception) {
            Timber.e(e, "Error getting ads")
            LoadResult.Error(e)
        }

    override fun getRefreshKey(state: PagingState<QueryDocumentSnapshot, DocumentSnapshot>): QueryDocumentSnapshot? = null
}
