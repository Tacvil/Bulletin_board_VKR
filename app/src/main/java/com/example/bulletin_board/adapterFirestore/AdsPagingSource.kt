package com.example.bulletin_board.adapterFirestore

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.bulletin_board.model.Ad
import com.example.bulletin_board.packroom.AdRepository
import com.google.firebase.firestore.DocumentSnapshot

class AdsPagingSource(
    private val adRepository: AdRepository,
    private val filters: MutableMap<String, String>,
) : PagingSource<DocumentSnapshot, Ad>() {
    override suspend fun load(params: LoadParams<DocumentSnapshot>): LoadResult<DocumentSnapshot, Ad> =
        try {
            val (ads, nextKey) =
                adRepository.getAllAds(
                    filters,
                    params.key,
                    params.loadSize.toLong(),
                )

            LoadResult.Page(ads, null, nextKey)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }

    override fun getRefreshKey(state: PagingState<DocumentSnapshot, Ad>): DocumentSnapshot? = null
}
