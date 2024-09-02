package com.example.bulletin_board.adapterFirestore

import android.content.Context
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.bulletin_board.Room.RemoteAdDataSource
import com.example.bulletin_board.Room.Result
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import kotlinx.coroutines.flow.first
import timber.log.Timber

class AdsPagingSource(
    private val remoteAdDataSource: RemoteAdDataSource,
    private val filter: MutableMap<String, String>,
    private val context: Context,
) : PagingSource<QueryDocumentSnapshot, DocumentSnapshot>() {
    override suspend fun load(params: LoadParams<QueryDocumentSnapshot>): LoadResult<QueryDocumentSnapshot, DocumentSnapshot> {
        try {
            val result =
                remoteAdDataSource
                    .getAllAds(context, filter, params.key?.toString())
                    .first()

            if (result is Result.Success) {
                val ads = result.data.first
                val nextKey = result.data.second
                return LoadResult.Page(ads, null, nextKey)
            } else if (result is Result.Error) {
                return LoadResult.Error(result.exception)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting ads")
            return LoadResult.Error(e)
        }
        return LoadResult.Error(Exception("Unknown error"))
    }

    override fun getRefreshKey(state: PagingState<QueryDocumentSnapshot, DocumentSnapshot>): QueryDocumentSnapshot? = null
}
