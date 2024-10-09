package com.example.bulletin_board.domain

import android.content.Context
import android.os.Parcelable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView

interface AdapterView {
    val nothinkWhiteAnim: LottieAnimationView
    val recyclerViewMainContent: RecyclerView
}

interface Adapter {
    fun refreshAdapter()

    val itemCountAdapter: Int
}

object AdapterManager {
    private val adapters = mutableMapOf<Int, Adapter>()

    fun registerAdapters(vararg adapters: Pair<Int, Adapter>) {
        adapters.forEach { (tabPosition, adapter) ->
            this.adapters[tabPosition] = adapter
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
        LoadingAnimationManager.updateAnimationVisibility(adapter.itemCountAdapter, adapterView)

        scrollStateMap[currentTabPosition] =
            adapterView.recyclerViewMainContent.layoutManager?.onSaveInstanceState()

        adapterView.recyclerViewMainContent.adapter = adapter as RecyclerView.Adapter<*>

        adapterView.recyclerViewMainContent.layoutManager?.onRestoreInstanceState(scrollStateMap[tabPosition])
    }
}
