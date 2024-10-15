package com.example.bulletin_board.presentation.adapter

import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bulletin_board.domain.model.Ad
import com.example.bulletin_board.domain.ui.adapters.AdapterView
import com.example.bulletin_board.presentation.utils.EmptyStateView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber

object PagingDataAdapterController {
    suspend fun handleAdapterData(
        dataFlow: Flow<PagingData<Ad>>,
        adapter: PagingDataAdapter<Ad, *>,
        adapterView: AdapterView,
    ) {
        setupAdapterObserver(adapter, adapterView)
        adapter.addLoadStateListener { loadStates ->
            if (loadStates.refresh is LoadState.NotLoading) {
                (adapterView.recyclerViewMainContent.layoutManager as? LinearLayoutManager)?.scrollToPosition(0)
            }
        }
        dataFlow
            .catch { e ->
                Timber.tag("MainActivity").e(e, "Error loading ads data")
            }.collectLatest { pagingData ->
                adapter.submitData(pagingData)
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
                    EmptyStateView.updateAnimationVisibility(
                        adapter.itemCount,
                        adapterView,
                    )
                }

                override fun onItemRangeRemoved(
                    positionStart: Int,
                    itemCount: Int,
                ) {
                    super.onItemRangeRemoved(positionStart, itemCount)
                    EmptyStateView.updateAnimationVisibility(
                        adapter.itemCount,
                        adapterView,
                    )
                }

                override fun onChanged() {
                    super.onChanged()
                    EmptyStateView.updateAnimationVisibility(
                        adapter.itemCount,
                        adapterView,
                    )
                }
            },
        )
        EmptyStateView.updateAnimationVisibility(adapter.itemCount, adapterView)
    }
}
