package com.example.bulletin_board.adapterFirestore

import android.content.Context
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.bulletin_board.packroom.RemoteAdDataSource
import com.google.firebase.firestore.DocumentSnapshot
import timber.log.Timber

class MyAdsPagingSource(
    private val remoteAdDataSource: RemoteAdDataSource,
    private val filter: MutableMap<String, String>,
    private val context: Context,
) : PagingSource<DocumentSnapshot, DocumentSnapshot>() {
    override suspend fun load(params: LoadParams<DocumentSnapshot>): LoadResult<DocumentSnapshot, DocumentSnapshot> =
        try {
            Timber.d("Paging: Loading ads : getAllAds")
            Timber.d("Paging: params.key : ${params.key}")
            val (ads, nextKey) = remoteAdDataSource.getAllAds(context, filter, params.key)

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

    override fun getRefreshKey(state: PagingState<DocumentSnapshot, DocumentSnapshot>): DocumentSnapshot? = null
}
