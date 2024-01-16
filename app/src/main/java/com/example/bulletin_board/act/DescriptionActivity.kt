package com.example.bulletin_board.act

import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.bulletin_board.R
import com.example.bulletin_board.adapters.ImageAdapter
import com.example.bulletin_board.databinding.ActivityDescriptionBinding
import com.example.bulletin_board.model.Announcement
import com.example.bulletin_board.utils.ImageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.Serializable

class DescriptionActivity : AppCompatActivity() {
    lateinit var binding: ActivityDescriptionBinding
    lateinit var adapter: ImageAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        getIntentFromMainAct()
    }

    private fun init(){
        adapter = ImageAdapter()
        binding.apply {
            viewPagerDescription.adapter = adapter
        }
    }

    private fun getIntentFromMainAct(){
        val ad = intent.serializable<Announcement>("AD")
        if (ad != null) fillImageArray(ad)
    }

    private inline fun <reified T : Serializable> Intent.serializable(key: String): T? = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getSerializableExtra(
            key,
            T::class.java
        )

        else -> @Suppress("DEPRECATION") getSerializableExtra(key) as? T
    }

    private fun fillImageArray(ad:Announcement){
        val listUris = listOf(ad.mainImage, ad.image2, ad.image3)
        CoroutineScope(Dispatchers.Main).launch {
            val bitMapList = ImageManager.getBitmapFromUris(listUris)
            adapter.update(bitMapList as ArrayList<Bitmap>)
        }
    }
}