package com.example.bulletin_board.adapterFirestore

import androidx.activity.result.launch
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.bulletin_board.model.Ad
import com.example.bulletin_board.model.AdUpdateEvent
import com.example.bulletin_board.packroom.AdRepository
import com.example.bulletin_board.viewmodel.FirebaseViewModel
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class AdsPagingSource(
    private val adRepository: AdRepository,
    private val viewModel: FirebaseViewModel,
) : PagingSource<DocumentSnapshot, Ad>() {
    init {
        viewModel.viewModelScope.launch {
            viewModel.filter.collectLatest {
                invalidate()
            }
            viewModel.adUpdated.collectLatest { event ->
                when (event) {
                    is AdUpdateEvent.FavUpdated -> {}
                    is AdUpdateEvent.ViewCountUpdated -> {}
                    is AdUpdateEvent.AdDeleted -> invalidate()
                }
            }
        }
    }

    override suspend fun load(params: LoadParams<DocumentSnapshot>): LoadResult<DocumentSnapshot, Ad> =
        try {
            Timber.d("Paging: Loading ads : getMyFavs")
            Timber.d("Paging: params.key : ${params.key}")
            val (ads, nextKey) =
                adRepository.getAllAds(
                    viewModel.filter.value,
                    params.key,
                    params.loadSize.toLong(),
                )

            LoadResult.Page(ads, null, nextKey)
        } catch (e: Exception) {
            Timber.e(e, "Error getting ads")
            LoadResult.Error(e)
        }

    override fun getRefreshKey(state: PagingState<DocumentSnapshot, Ad>): DocumentSnapshot? = null
}
