package com.example.bulletin_board.domain.ui.adapters

interface ItemTouchAdapter {
    fun onMove(
        startPos: Int,
        targetPos: Int,
    )

    fun onClear()
}
