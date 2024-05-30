package com.example.bulletin_board.application

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.example.bulletin_board.R
import com.example.bulletin_board.settings.LocaleManager
class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
    }
}