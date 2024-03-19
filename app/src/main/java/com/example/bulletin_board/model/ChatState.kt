package com.example.bulletin_board.model

data class ChatState (
    val isEnteringToken: Boolean = true,
    val remoteToken: String = "",
    val messageText:String = ""
)