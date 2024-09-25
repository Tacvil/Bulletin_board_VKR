package com.example.bulletin_board.adapterFirestore

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.bulletin_board.model.Ad
import com.example.bulletin_board.model.AdUpdateEvent
import com.example.bulletin_board.packroom.AdRepository
import com.example.bulletin_board.viewmodel.FirebaseViewModel
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import timber.log.Timber

class AdsPagingSource(
    private val adRepository: AdRepository,
    private val viewModel: FirebaseViewModel,
) : PagingSource<DocumentSnapshot, Ad>() {
    init {
        Timber.d("Paging: AdsPagingSource init")
        viewModel.viewModelScope.launch {
            viewModel.appState.drop(1).collectLatest { event ->
                when (event.adEvent) {
                    is AdUpdateEvent.FavUpdated -> {}
                    is AdUpdateEvent.ViewCountUpdated -> {}
                    is AdUpdateEvent.AdDeleted -> invalidate()
                    is AdUpdateEvent.Initial -> {}
                }
                if (event.filter.isNotEmpty()) {
                    Timber.d("Paging: filter changed : ${event.filter}")
                    invalidate()
                }
            }
        }
    }

    override suspend fun load(params: LoadParams<DocumentSnapshot>): LoadResult<DocumentSnapshot, Ad> =
        try {
            Timber.d("Paging: Loading ads : getMyFavs")
            Timber.d("Paging: params.key : ${params.key}")
            Timber.d("filter : ${viewModel.appState.value.filter}")
            val (ads, nextKey) =
                adRepository.getAllAds(
                    viewModel.appState.value.filter,
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
