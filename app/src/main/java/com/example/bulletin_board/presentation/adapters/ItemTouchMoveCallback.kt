package com.example.bulletin_board.presentation.adapters

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.bulletin_board.domain.ui.adapters.ItemTouchAdapter

class ItemTouchMoveCallback(
    val adapter: ItemTouchAdapter,
) : ItemTouchHelper.Callback() {
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
    ): Int {
        val dragFlag = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        return makeMovementFlags(dragFlag, SWIPE_FLAGS)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder,
    ): Boolean {
        adapter.onMove(viewHolder.absoluteAdapterPosition, target.absoluteAdapterPosition)
        return true
    }

    override fun onSwiped(
        viewHolder: RecyclerView.ViewHolder,
        direction: Int,
    ) {
    }

    override fun onSelectedChanged(
        viewHolder: RecyclerView.ViewHolder?,
        actionState: Int,
    ) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE)viewHolder?.itemView?.alpha = ALPHA_SELECTED
        super.onSelectedChanged(viewHolder, actionState)
    }

    override fun clearView(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
    ) {
        viewHolder.itemView.alpha = ALPHA_DEFAULT
        adapter.onClear()
        super.clearView(recyclerView, viewHolder)
    }

    companion object {
        private const val ALPHA_SELECTED = 0.5f
        private const val ALPHA_DEFAULT = 1.0f
        private const val SWIPE_FLAGS = 0
    }
}
