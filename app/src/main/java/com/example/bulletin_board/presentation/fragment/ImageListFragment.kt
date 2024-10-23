package com.example.bulletin_board.presentation.fragment

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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bulletin_board.R
import com.example.bulletin_board.data.image.ImageManager
import com.example.bulletin_board.data.image.PixImagePicker.Companion.MAX_IMAGE_COUNT
import com.example.bulletin_board.databinding.FragmentImageListBinding
import com.example.bulletin_board.domain.navigation.OnFragmentClosedListener
import com.example.bulletin_board.presentation.adapters.ItemTouchMoveCallback
import com.example.bulletin_board.presentation.adapters.SelectImageRvAdapter
import com.example.bulletin_board.presentation.dialogs.ProgressDialog
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject

@AndroidEntryPoint
class ImageListFragment
    @Inject
    constructor(
        private val onFragmentClosedListener: OnFragmentClosedListener,
        private val imageManager: ImageManager,
        private val adapter: SelectImageRvAdapter,
    ) : BaseAdsFrag() {
        private var _binding: FragmentImageListBinding? = null
        val binding get() = _binding!!

        private lateinit var dragCallback: ItemTouchMoveCallback
        private lateinit var touchHelper: ItemTouchHelper
        var addImageMenuItem: MenuItem? = null

        private var onBindingReady: (() -> Unit)? = null

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
        ): View {
            _binding = FragmentImageListBinding.inflate(inflater, container, false)
            bannerAdView = binding.bannerAdView
            return binding.root
        }

        override fun onViewCreated(
            view: View,
            savedInstanceState: Bundle?,
        ) {
            super.onViewCreated(view, savedInstanceState)
            initializeRecyclerView()
            initializeToolbar()
            onBindingReady?.invoke()
        }

        private fun initializeRecyclerView() {
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

        private fun initializeToolbar() {
            binding.toolbar.apply {
                inflateMenu(R.menu.menu_choose_image)
                setNavigationOnClickListener {
                    activity
                        ?.supportFragmentManager
                        ?.beginTransaction()
                        ?.remove(this@ImageListFragment)
                        ?.commit()
                    onFragmentClosedListener.onFragClose(adapter.selectedImages)
                }

                menu.apply {
                    addImageMenuItem =
                        findItem(R.id.id_add_image).apply {
                            isVisible = adapter.selectedImages.size <= 2
                        }
                    findItem(R.id.id_delete_image).setOnMenuItemClickListener {
                        adapter.updateSelectedImages(emptyList(), true)
                        addImageMenuItem?.isVisible = true
                        true
                    }
                    addImageMenuItem?.setOnMenuItemClickListener {
                        imageManager.addImages(MAX_IMAGE_COUNT - adapter.selectedImages.size)
                        true
                    }
                }
            }
        }

        fun updateSelectedImagesFromEdit(bitmapList: List<Bitmap>) {
            adapter.updateSelectedImages(bitmapList, true)
        }

        fun updateAdapter(
            imageUris: ArrayList<Uri>,
            activity: Activity,
        ) {
            imageUris.takeIf { it.isNotEmpty() }?.let {
                resizeAndDisplaySelectedImages(it, adapter.selectedImages.isEmpty(), activity)
            }
        }

        fun setSingleImage(
            uri: Uri,
            pos: Int,
        ) {
            val recyclerView = binding.recyclerViewSelectImage
            recyclerView.post {
                if (recyclerView.adapter != null && recyclerView.adapter!!.itemCount > pos) {
                    val progressBar =
                        recyclerView[pos]
                            .findViewById<ProgressBar>(R.id.progress_bar_item_loading)
                            .apply { visibility = View.VISIBLE }
                    imageManager.imageResize(arrayListOf(uri)) { bitmapList ->
                        adapter.selectedImages[pos] = bitmapList[0]
                        progressBar.visibility = View.GONE
                        adapter.notifyItemChanged(pos)
                    }
                }
            }
        }

        fun resizeAndDisplaySelectedImages(
            imageUris: ArrayList<Uri>,
            shouldClearExistingImages: Boolean,
            activity: Activity,
        ) {
            val dialog = ProgressDialog.createProgressDialog(activity)
            imageManager.imageResize(imageUris) { bitmapList ->
                adapter.updateSelectedImages(bitmapList, shouldClearExistingImages)
                dialog.dismiss()
                updateAddImageMenuItemVisibility()
            }
        }

        private fun updateAddImageMenuItemVisibility() {
            addImageMenuItem?.isVisible = adapter.selectedImages.size <= 2
        }

        fun setOnBindingReadyListener(listener: (() -> Unit)?) {
            onBindingReady = listener
        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }
    }
