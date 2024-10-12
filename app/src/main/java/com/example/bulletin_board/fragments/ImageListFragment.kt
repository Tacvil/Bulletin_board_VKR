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
import com.example.bulletin_board.utils.ItemTouchMoveCallback
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject

interface EditImagePosListener {
    fun updateEditImagePos(pos: Int)
}

@AndroidEntryPoint
class ImageListFragment
    @Inject
    constructor(
        private val fragCloseInterface: FragmentCloseInterface,
        private val editImagePosListener: EditImagePosListener,
        private val imageManager: ImageManager,
        private val glideImageLoader: GlideImageLoader,
    ) : BaseAdsFrag(),
        ImageAdapterHandler {
        private val adapter: SelectImageRvAdapter by lazy {
            SelectImageRvAdapter(
                { addItem?.isVisible = true },
                this,
                glideImageLoader,
            )
        }
        private lateinit var dragCallback: ItemTouchMoveCallback
        private lateinit var touchHelper: ItemTouchHelper
        private var addItem: MenuItem? = null
        private lateinit var binding: ListImageFragBinding

        // Lifecycle methods
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
            setupRecyclerView()
            setUpToolbar()
        }

        private fun setupRecyclerView() {
            dragCallback = ItemTouchMoveCallback(adapter)
            touchHelper =
                ItemTouchHelper(dragCallback).apply {
                    attachToRecyclerView(binding.recyclerViewSelectImage)
                }
            binding.recyclerViewSelectImage.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = this@ImageListFragment.adapter
            }
        }

        private fun setUpToolbar() {
            binding.toolbar.apply {
                inflateMenu(R.menu.menu_choose_image)
                setNavigationOnClickListener {
                    activity
                        ?.supportFragmentManager
                        ?.beginTransaction()
                        ?.remove(this@ImageListFragment)
                        ?.commit()
                    fragCloseInterface.onFragClose(adapter.mainArray)
                }

                menu.apply {
                    addItem = findItem(R.id.id_add_image).apply { isVisible = adapter.mainArray.size <= 2 }
                    findItem(R.id.id_delete_image).setOnMenuItemClickListener {
                        adapter.updateAdapter(emptyList(), true)
                        addItem?.isVisible = true
                        true
                    }
                    addItem?.setOnMenuItemClickListener {
                        imageManager.addImages(MAX_IMAGE_COUNT - adapter.mainArray.size)
                        true
                    }
                }
            }
        }

        // Adapter update methods
        fun updateAdapterFromEdit(bitmapList: List<Bitmap>) {
            adapter.updateAdapter(bitmapList, true)
        }

        fun updateAdapter(
            newList: ArrayList<Uri>,
            activity: Activity,
        ) {
            newList.takeIf { it.isNotEmpty() }?.let {
                resizeSelectedImages(it, adapter.mainArray.isEmpty(), activity)
            }
        }

        fun setSingleImage(
            uri: Uri,
            pos: Int,
        ) {
            val pBar =
                binding.recyclerViewSelectImage[pos].findViewById<ProgressBar>(R.id.progress_bar).apply {
                    visibility = View.VISIBLE
                }
            imageManager.imageResize(arrayListOf(uri)) { bitmapList ->
                adapter.mainArray[pos] = bitmapList[0]
                pBar.visibility = View.GONE
                adapter.notifyItemChanged(pos)
            }
        }

        fun resizeSelectedImages(
            newList: ArrayList<Uri>,
            needClear: Boolean,
            activity: Activity,
        ) {
            val dialog = ProgressDialog.createProgressDialog(activity)
            imageManager.imageResize(newList) { bitmapList ->
                adapter.updateAdapter(bitmapList, needClear)
                dialog.dismiss()
                updateAddItemVisibility()
            }
        }

        private fun updateAddItemVisibility() {
            addItem?.isVisible = adapter.mainArray.size <= 2
        }

        // ImageAdapterHandler implementation
        override fun getTitle(position: Int): String = resources.getStringArray(R.array.title_array)[position]

        override fun getSingleImages(editImagePos: Int) {
            editImagePosListener.updateEditImagePos(editImagePos)
            imageManager.getSingleImages()
        }

        override fun chooseScaleType(bitmap: Bitmap): Boolean = imageManager.chooseScaleType(bitmap)
    }
