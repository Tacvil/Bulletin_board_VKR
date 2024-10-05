package com.example.bulletin_board.domain

import androidx.paging.PagingData
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bulletin_board.model.Ad
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber

object DataAdapterManager {
    suspend fun handleAdapterData(
        dataFlow: Flow<PagingData<Ad>>,
        adapter: PagingDataAdapter<Ad, *>,
        adapterView: AdapterView,
    ) {
        setupAdapterObserver(adapter, adapterView)

        dataFlow
            .catch { e ->
                Timber.tag("MainActivity").e(e, "Error loading ads data")
            }.collectLatest { pagingData ->
                adapter.submitData(pagingData)
                LoadingAnimationManager.updateAnimationVisibility(adapter.itemCount, adapterView)
            }
    }

    private fun setupAdapterObserver(
        adapter: PagingDataAdapter<Ad, *>,
        adapterView: AdapterView,
    ) {
        adapter.registerAdapterDataObserver(
            object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(
                    positionStart: Int,
                    itemCount: Int,
                ) {
                    super.onItemRangeInserted(positionStart, itemCount)
                    LoadingAnimationManager.updateAnimationVisibility(adapter.itemCount, adapterView)
                }

                override fun onItemRangeRemoved(
                    positionStart: Int,
                    itemCount: Int,
                ) {
                    super.onItemRangeRemoved(positionStart, itemCount)
                    LoadingAnimationManager.updateAnimationVisibility(adapter.itemCount, adapterView)
                }

                override fun onChanged() {
                    super.onChanged()
                    LoadingAnimationManager.updateAnimationVisibility(adapter.itemCount, adapterView)
                }
            },
        )
        LoadingAnimationManager.updateAnimationVisibility(adapter.itemCount, adapterView)
    }
}
