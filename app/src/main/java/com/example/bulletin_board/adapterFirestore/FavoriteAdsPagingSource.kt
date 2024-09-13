package com.example.bulletin_board.adapterFirestore

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.bulletin_board.model.Ad
import com.example.bulletin_board.packroom.RemoteAdDataSource
import com.google.firebase.firestore.DocumentSnapshot
import timber.log.Timber

class FavoriteAdsPagingSource(
    private val remoteAdDataSource: RemoteAdDataSource,
) : PagingSource<DocumentSnapshot, Ad>() {
    override suspend fun load(params: LoadParams<DocumentSnapshot>): LoadResult<DocumentSnapshot, Ad> =
        try {
            Timber.d("Paging: Loading ads : getMyFavs")
            Timber.d("Paging: params.key : ${params.key}")
            val (ads, nextKey) = remoteAdDataSource.getMyFavs(params.loadSize.toLong(), params.key)

            LoadResult.Page(ads, null, nextKey)
        } catch (e: Exception) {
            Timber.e(e, "Error getting ads")
            LoadResult.Error(e)
        }

    override fun getRefreshKey(state: PagingState<DocumentSnapshot, Ad>): DocumentSnapshot? = null
}
