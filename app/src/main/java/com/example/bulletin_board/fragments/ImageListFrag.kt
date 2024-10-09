package com.example.bulletin_board.fragments

import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.view.get
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.bulletin_board.R
import com.example.bulletin_board.databinding.ListImageFragBinding
import com.example.bulletin_board.dialoghelper.ProgressDialog
import com.example.bulletin_board.domain.ImageLoader
import com.example.bulletin_board.domain.image.ImageManager
import com.example.bulletin_board.domain.image.PixImagePicker.Companion.MAX_IMAGE_COUNT
import com.example.bulletin_board.utils.AdapterCallback
import com.example.bulletin_board.utils.ItemTouchMoveCallback
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject

interface EditImagePosListener {
    fun updateEditImagePos(pos: Int)
}

@AndroidEntryPoint
class ImageListFrag(
    private val fragCloseInterface: FragmentCloseInterface,
    private val editImagePosListener: EditImagePosListener,
) : BaseAdsFrag(),
    AdapterCallback,
    ImageLoader,
    TitleProvider,
    GetSingleImagesHandler,
    ChooseScaleTypeHandler {
    val adapter =
        SelectImageRvAdapter(
            this,
            this,
            this,
            this,
            this,
        )
    private val dragCallback = ItemTouchMoveCallback(adapter)
    private val touchHelper = ItemTouchHelper(dragCallback)
    private var addItem: MenuItem? = null
    lateinit var binding: ListImageFragBinding

    @Inject
    lateinit var imageManager: ImageManager

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
        needClear: Boolean,
        activity: Activity,
    ) {
        val dialog = ProgressDialog.createProgressDialog(activity)
        imageManager.imageResize(newList) { bitmapList ->
            adapter.updateAdapter(bitmapList, needClear)
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
        resizeSelectedImages(newList, false, activity)
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

    override fun loadImage(
        imageView: ImageView,
        imageUrl: Any?,
        requestOptions: RequestOptions,
    ) {
        when (imageUrl) {
            is Bitmap -> {
                Glide
                    .with(this)
                    .load(imageUrl)
                    .apply(requestOptions)
                    .into(imageView)
            }

            is Uri -> {
                Glide
                    .with(this)
                    .load(imageUrl)
                    .apply(requestOptions)
                    .into(imageView)
            }
        }
    }

    override fun getTitle(position: Int): String = resources.getStringArray(R.array.title_array)[position]

    override fun getSingleImages(editImagePos: Int) {
        editImagePosListener.updateEditImagePos(editImagePos)
        imageManager.getSingleImages()
    }

    override fun chooseScaleType(bitmap: Bitmap): Boolean = imageManager.chooseScaleType(bitmap)
}
