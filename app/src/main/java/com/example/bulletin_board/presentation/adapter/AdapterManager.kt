package com.example.bulletin_board.presentation.adapter

import android.content.Context
import android.os.Parcelable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bulletin_board.domain.ui.adapters.Adapter
import com.example.bulletin_board.domain.ui.adapters.AdapterView
import com.example.bulletin_board.presentation.utils.EmptyStateView

object AdapterManager {
    private val adapters = mutableMapOf<Int, Adapter>()

    fun registerAdapters(vararg adapters: Pair<Int, Adapter>) {
        adapters.forEach { (tabPosition, adapter) ->
            AdapterManager.adapters[tabPosition] = adapter
        }
    }

    fun refreshAdapter(tabPosition: Int) {
        adapters[tabPosition]?.refreshAdapter()
    }

    fun initRecyclerView(
        recyclerView: RecyclerView,
        adapter: RecyclerView.Adapter<*>,
        context: Context,
    ) {
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    fun switchAdapter(
        adapterView: AdapterView,
        tabPosition: Int,
        scrollStateMap: MutableMap<Int, Parcelable?>,
        currentTabPosition: Int,
    ) {
        val adapter = adapters[tabPosition] ?: return
        EmptyStateView.updateAnimationVisibility(adapter.itemCountAdapter, adapterView)

        scrollStateMap[currentTabPosition] =
            adapterView.recyclerViewMainContent.layoutManager?.onSaveInstanceState()

        adapterView.recyclerViewMainContent.adapter = adapter as RecyclerView.Adapter<*>

        adapterView.recyclerViewMainContent.layoutManager?.onRestoreInstanceState(scrollStateMap[tabPosition])
    }
}
