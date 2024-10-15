package com.example.bulletin_board.domain.ui.adapters

import com.example.bulletin_board.domain.model.AdUpdateEvent

interface AppStateListener {
    fun onAppStateEvent(adEvent: (AdUpdateEvent) -> Unit)
}
