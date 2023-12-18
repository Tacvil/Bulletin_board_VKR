package com.example.bulletin_board.fragments

import android.app.Activity
import android.graphics.Bitmap
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
import com.example.bulletin_board.act.EditAdsActivity
import com.example.bulletin_board.databinding.ListImageFragBinding
import com.example.bulletin_board.dialoghelper.ProgressDialog
import com.example.bulletin_board.utils.AdapterCallback
import com.example.bulletin_board.utils.ImageManager
import com.example.bulletin_board.utils.ImagePicker
import com.example.bulletin_board.utils.ItemTouchMoveCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ImageListFrag(private val fragCloseInterface : FragmentCloseInterface, private val newList : ArrayList<String>?) : BaseAdsFrag(), AdapterCallback {

    val adapter = SelectImageRvAdapter(this)
    val dragCallback = ItemTouchMoveCallback(adapter)
    val touchHelper = ItemTouchHelper(dragCallback)
    private var addItem: MenuItem? = null
    private var job: Job? = null
    lateinit var binding: ListImageFragBinding
    val istanceBaseAds = BaseAdsFrag()
    //
    var onLoadClickListener: (() -> Unit)? = null
    //
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ListImageFragBinding.inflate(layoutInflater)
        mBannerAdView = binding.adView
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpToolbar()

        touchHelper.attachToRecyclerView(binding.recyclerViewSelectImage)
        binding.recyclerViewSelectImage.layoutManager = LinearLayoutManager(activity)
        binding.recyclerViewSelectImage.adapter = adapter

        if (newList != null) resizeSelectedImages(newList, true)

    }

    fun updateAdapterFromEdit(bitmapList: List<Bitmap>){
        adapter.updateAdapter(bitmapList, true)

    }

    override fun onDetach() {
        super.onDetach()
        fragCloseInterface.onFragClose(adapter.mainArray)
        job?.cancel()
    }
//
//    override fun onClose() {
//        super.onClose()
//        activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
//    }
//
    private fun resizeSelectedImages(newList: ArrayList<String>, needClear: Boolean){
        job = CoroutineScope(Dispatchers.Main).launch {
            val dialog = ProgressDialog.createProgressDialog(activity as Activity)
            val bitmapList = ImageManager.imageResize(newList)
            adapter.updateAdapter(bitmapList, needClear)
            dialog.dismiss()
            //Log.d("MyLog", "Result : $text")
            if (adapter.mainArray.size > 2) addItem?.isVisible = false
        }
    }

    private fun setUpToolbar(){
        binding.toolbar.inflateMenu(R.menu.menu_choose_image)
        val deleteItem = binding.toolbar.menu.findItem(R.id.id_delete_image)
        addItem = binding.toolbar.menu.findItem(R.id.id_add_image)

        binding.toolbar.setNavigationOnClickListener {

            //istanceBaseAds.loadInterstitial()
            //onLoadClickListener?.invoke()
            activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
        }

        deleteItem.setOnMenuItemClickListener {
            adapter.updateAdapter(ArrayList(), true)
            addItem?.isVisible = true
            true
        }

        addItem?.setOnMenuItemClickListener {
            val imageCount = ImagePicker.MAX_IMAGE_COUNT - adapter.mainArray.size
            ImagePicker.launcher(activity as EditAdsActivity, (activity as EditAdsActivity).launcherMultiSelectImage, imageCount)
            true
        }

    }

    fun updateAdapter(newList: ArrayList<String>){
        resizeSelectedImages(newList, false)
    }

    fun setSingleImage(uri: String, pos: Int){
        val pBar = binding.recyclerViewSelectImage[pos].findViewById<ProgressBar>(R.id.progress_bar)
        job = CoroutineScope(Dispatchers.Main).launch {
            pBar.visibility = View.VISIBLE
            val bitmapList = ImageManager.imageResize(listOf(uri))
            pBar.visibility = View.GONE
            adapter.mainArray[pos] = bitmapList[0]
            adapter.notifyItemChanged(pos)
        }
    }

    override fun onItemDelete() {
        addItem?.isVisible = true
    }
}