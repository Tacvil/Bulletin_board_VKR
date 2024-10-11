package com.example.bulletin_board.fragments

import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bulletin_board.R
import com.example.bulletin_board.databinding.ListImageFragBinding
import com.example.bulletin_board.dialoghelper.ProgressDialog
import com.example.bulletin_board.domain.image.GlideImageLoader
import com.example.bulletin_board.domain.image.ImageManager
import com.example.bulletin_board.domain.image.PixImagePicker.Companion.MAX_IMAGE_COUNT
import com.example.bulletin_board.utils.AdapterCallback
import com.example.bulletin_board.utils.ItemTouchMoveCallback
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import timber.log.Timber

interface EditImagePosListener {
    fun updateEditImagePos(pos: Int)
}

@AndroidEntryPoint
class ImageListFrag
    @Inject
    constructor(
        private val fragCloseInterface: FragmentCloseInterface,
        private val editImagePosListener: EditImagePosListener,
        private val imageManager: ImageManager,
        private val glideImageLoader: GlideImageLoader,
    ) : BaseAdsFrag(),
        AdapterCallback,
        TitleProvider,
        GetSingleImagesHandler,
        ChooseScaleTypeHandler {
        private val adapter: SelectImageRvAdapter by lazy {
            SelectImageRvAdapter(
                this,
                this,
                this,
                this,
                glideImageLoader,
            )
        }
        private lateinit var dragCallback: ItemTouchMoveCallback
        private lateinit var touchHelper: ItemTouchHelper

        private var addItem: MenuItem? = null
        lateinit var binding: ListImageFragBinding

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
        ): View {
            binding = ListImageFragBinding.inflate(layoutInflater, container, false)
            mBannerAdView = binding.adView
            return binding.root
        }

        override fun onViewCreated(
            view: View,
            savedInstanceState: Bundle?,
        ) {
            super.onViewCreated(view, savedInstanceState)

            dragCallback = ItemTouchMoveCallback(adapter)
            touchHelper = ItemTouchHelper(dragCallback)

            setUpToolbar()

            touchHelper.attachToRecyclerView(binding.recyclerViewSelectImage)
            binding.recyclerViewSelectImage.layoutManager = LinearLayoutManager(activity)
            binding.recyclerViewSelectImage.adapter = adapter
        }

        fun updateAdapterFromEdit(bitmapList: List<Bitmap>) {
            adapter.updateAdapter(bitmapList, true)
        }

        fun resizeSelectedImages(
            newList: ArrayList<Uri>,
            needClear: Boolean, // Флаг, указывающий на добавление или замену
            activity: Activity,
        ) {
            val dialog = ProgressDialog.createProgressDialog(activity)
            imageManager.imageResize(newList) { bitmapList ->
                Timber.d("needClear $needClear")
                Timber.d("adapter ${adapter.mainArray}")
                if (needClear) {
                    adapter.updateAdapter(bitmapList, true) // Заменяем, если пусто
                } else {
                    adapter.updateAdapter(bitmapList, false)
                }
                dialog.dismiss()
                if (adapter.mainArray.size > 2) addItem?.isVisible = false
            }
        }

        private fun setUpToolbar() {
            binding.toolbar.inflateMenu(R.menu.menu_choose_image)
            val deleteItem = binding.toolbar.menu.findItem(R.id.id_delete_image)
            addItem = binding.toolbar.menu.findItem(R.id.id_add_image)
            if (adapter.mainArray.size > 2) addItem?.isVisible = false

            binding.toolbar.setNavigationOnClickListener {
                activity
                    ?.supportFragmentManager
                    ?.beginTransaction()
                    ?.remove(this)
                    ?.commit()
                fragCloseInterface.onFragClose(adapter.mainArray)
            }

            deleteItem.setOnMenuItemClickListener {
                adapter.updateAdapter(ArrayList(), true)
                addItem?.isVisible = true
                true
            }

            addItem?.setOnMenuItemClickListener {
                val imageCount = MAX_IMAGE_COUNT - adapter.mainArray.size
                imageManager.addImages(imageCount)
                true
            }
        }

        fun updateAdapter(
            newList: ArrayList<Uri>,
            activity: Activity,
        ) {
            // Проверяем, нужно ли добавлять или заменять
            val needClear = adapter.mainArray.isEmpty()
            resizeSelectedImages(newList, needClear, activity)
        }

        fun setSingleImage(
            uri: Uri,
            pos: Int,
        ) {
            val pBar = binding.recyclerViewSelectImage[pos].findViewById<ProgressBar>(R.id.progress_bar)

            pBar.visibility = View.VISIBLE
            imageManager.imageResize(arrayListOf(uri)) { bitmapList ->
                adapter.mainArray[pos] = bitmapList[0]
                pBar.visibility = View.GONE
                adapter.notifyItemChanged(pos)
            }
        }

        override fun onItemDelete() {
            addItem?.isVisible = true
        }

        override fun getTitle(position: Int): String = resources.getStringArray(R.array.title_array)[position]

        override fun getSingleImages(editImagePos: Int) {
            editImagePosListener.updateEditImagePos(editImagePos)
            imageManager.getSingleImages()
        }

        override fun chooseScaleType(bitmap: Bitmap): Boolean = imageManager.chooseScaleType(bitmap)
    }
