package com.example.bulletin_board.presentation.adapters

import android.content.Context
import android.os.Parcelable
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bulletin_board.domain.ui.adapters.AdapterView
import com.example.bulletin_board.presentation.utils.EmptyStateView

object AdapterManager {
    private val adapters: MutableMap<Int, PagingDataAdapter<*, *>> = mutableMapOf()

    private var currentAdapterId: Int = 0 // Initialize with default value

    fun registerAdapters(vararg adapterPairs: Pair<Int, PagingDataAdapter<*, *>>) {
        for (adapterPair in adapterPairs) {
            adapters[adapterPair.first] = adapterPair.second
        }
    }

    fun getCurrentAdapter(): PagingDataAdapter<*, *>? {
        return adapters[currentAdapterId]
    }

    fun initRecyclerView(
        recyclerView: RecyclerView,
        adapter: RecyclerView.Adapter<*>,
        context: Context,
    ) {
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    fun refreshAdapters() {
        for (adapter in adapters.values) {
            adapter.refresh()
        }
    }

    fun switchAdapter(
        adapterView: AdapterView,
        scrollStateMap: MutableMap<Int, Parcelable?>,
        currentTabPosition: Int,
        tabPosition: Int,
    ) {
        val adapter = adapters[tabPosition]
        currentAdapterId = tabPosition

        EmptyStateView.updateAnimationVisibility(adapterView)

        scrollStateMap[currentTabPosition] = adapterView.recyclerViewMainContent.layoutManager?.onSaveInstanceState()

        adapterView.recyclerViewMainContent.adapter = adapter

        adapterView.recyclerViewMainContent.layoutManager?.onRestoreInstanceState(scrollStateMap[tabPosition])
    }
}
