package com.example.bulletin_board.domain.images

import android.content.ContentResolver

interface ContentResolverProvider {
    fun getContentResolverAct(): ContentResolver
}
