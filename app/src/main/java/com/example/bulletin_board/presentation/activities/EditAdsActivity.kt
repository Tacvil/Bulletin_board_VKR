package com.example.bulletin_board.presentation.activities

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import com.example.bulletin_board.R
import com.example.bulletin_board.data.image.ImageManager
import com.example.bulletin_board.data.image.PixImagePicker.Companion.MAX_IMAGE_COUNT
import com.example.bulletin_board.data.utils.LocaleManager
import com.example.bulletin_board.data.utils.SortUtils
import com.example.bulletin_board.databinding.ActivityEditAdsBinding
import com.example.bulletin_board.domain.auth.impl.AccountManager
import com.example.bulletin_board.domain.images.ContentResolverProvider
import com.example.bulletin_board.domain.images.PixImagePickerActions
import com.example.bulletin_board.domain.images.ViewModelHandler
import com.example.bulletin_board.domain.location.CityDataSourceProvider
import com.example.bulletin_board.domain.model.Ad
import com.example.bulletin_board.domain.navigation.OnFragmentClosedListener
import com.example.bulletin_board.domain.ui.adapters.ImageAdapterHandler
import com.example.bulletin_board.domain.ui.adapters.OnItemDeleteListener
import com.example.bulletin_board.domain.utils.ToastHelper
import com.example.bulletin_board.pix.models.Options
import com.example.bulletin_board.presentation.adapters.ImageAdapter
import com.example.bulletin_board.presentation.adapters.RcViewDialogSpinnerAdapter
import com.example.bulletin_board.presentation.dialogs.DialogSpinnerHelper
import com.example.bulletin_board.presentation.fragment.ImageListFragment
import com.example.bulletin_board.presentation.theme.ThemeManager
import com.example.bulletin_board.presentation.utils.KeyboardUtils
import com.example.bulletin_board.presentation.viewModel.MainViewModel
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import io.ak1.pix.helpers.PixEventCallback
import io.ak1.pix.helpers.addPixToActivity
import io.ak1.pix.helpers.showStatusBar
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditAdsActivity
    @Inject
    constructor() :
    AppCompatActivity(),
        OnFragmentClosedListener,
        ToastHelper,
        ViewModelHandler,
        ContentResolverProvider,
        PixImagePickerActions,
        OnItemDeleteListener,
        ImageAdapterHandler {
        private val viewModel: MainViewModel by viewModels()
        private lateinit var binding: ActivityEditAdsBinding

        @Inject
        lateinit var imageListFragment: ImageListFragment

        @Inject
        lateinit var accountManager: AccountManager

        @Inject
        lateinit var imageManager: ImageManager

        @Inject
        lateinit var imageAdapter: ImageAdapter

        @Inject
        lateinit var cityDataSourceProvider: CityDataSourceProvider

        @Inject
        lateinit var dialogSpinnerHelper: DialogSpinnerHelper

        @Inject
        lateinit var sortUtils: SortUtils

        private var selectedImagePosition = 0
        private var isInEditMode = false
        private var currentAd: Ad? = null
        private var focusedEditText: TextInputEditText? = null

        override fun attachBaseContext(newBase: Context) {
            super.attachBaseContext(LocaleManager.setLocale(newBase))
        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun onCreate(savedInstanceState: Bundle?) {
            setTheme(ThemeManager.getSelectedTheme(PreferenceManager.getDefaultSharedPreferences(this)))
            super.onCreate(savedInstanceState)
            binding = ActivityEditAdsBinding.inflate(layoutInflater)
            setContentView(binding.root)

            initializeComponents()
            handleEditMode()
            setupSelectors()
            handleImageSelection()
            setupPublishButton()
            imageChangeCounter()
        }

        private fun initializeComponents() {
            binding.imageViewPager.adapter = imageAdapter
        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        private fun handleEditMode() {
            if (isInEditMode()) {
                isInEditMode = true
                intent.getParcelableExtra(MainActivity.EXTRA_AD_ITEM, Ad::class.java)?.let {
                    currentAd = it
                    populateFields(it)
                }
            }
        }

        private fun setupSelectors() {
            with(binding) {
                val editTexts =
                    listOf(
                        adTitleEditText,
                        indexEditText,
                        phoneEditText,
                        emailEditText,
                        priceEditText,
                        descriptionEditText,
                    )

                editTexts.forEach { editText ->
                    editText.onFocusChangeListener =
                        View.OnFocusChangeListener { v, hasFocus ->
                            if (hasFocus) {
                                focusedEditText = v as? TextInputEditText
                            }
                        }
                }

                selectCountryEditText.setOnClickListener {
                    focusedEditText?.clearFocus()
                    KeyboardUtils.hideKeyboard(this@EditAdsActivity, selectCountryEditText)
                    if (selectCityEditText.text.toString() != EMPTY_STRING) {
                        selectCityEditText.setText(EMPTY_STRING)
                    }
                    showSpinnerPopup(selectCountryEditText, cityDataSourceProvider.getAllCountries()) {
                        selectCountryEditText.setText(it)
                    }
                }

                selectCityEditText.setOnClickListener {
                    focusedEditText?.clearFocus()
                    KeyboardUtils.hideKeyboard(this@EditAdsActivity, selectCityEditText)
                    val selectedCountry = selectCountryEditText.text.toString()
                    if (selectedCountry.isNotBlank()) {
                        showSpinnerPopup(
                            selectCityEditText,
                            cityDataSourceProvider.getAllCities(selectedCountry),
                        ) {
                            selectCityEditText.setText(it)
                        }
                    } else {
                        Toast
                            .makeText(
                                this@EditAdsActivity,
                                getString(R.string.edit_no_country_selected),
                                Toast.LENGTH_SHORT,
                            ).show()
                    }
                }

                selectSendOptionEditText.setOnClickListener {
                    focusedEditText?.clearFocus()
                    KeyboardUtils.hideKeyboard(this@EditAdsActivity, selectSendOptionEditText)
                    val deliveryOptionsList =
                        arrayListOf(
                            Pair(getString(R.string.no_matter), EMPTY_STRING),
                            Pair(getString(R.string.with_sending), EMPTY_STRING),
                            Pair(getString(R.string.without_sending), EMPTY_STRING),
                        )
                    showSpinnerPopup(selectSendOptionEditText, deliveryOptionsList, false) { item ->
                        selectSendOptionEditText.setText(item)
                    }
                }

                selectCategoryEditText.setOnClickListener {
                    focusedEditText?.clearFocus()
                    KeyboardUtils.hideKeyboard(this@EditAdsActivity, selectCategoryEditText)
                    val listCategory = resources.getStringArray(R.array.category)
                    val pairsCategory =
                        ArrayList<Pair<String, String>>(listCategory.map { Pair(it, EMPTY_STRING) })
                    showSpinnerPopup(selectCategoryEditText, pairsCategory, false) { item ->
                        selectCategoryEditText.setText(item)
                    }
                }
            }
        }

        private fun handleImageSelection() {
            binding.addImageButton.setOnClickListener {
                if (imageAdapter.imageBitmapList.size == 0) {
                    imageManager.getMultiImages(MAX_IMAGE_COUNT)
                } else {
                    showImageListFrag(null)
                    imageListFragment.updateSelectedImagesFromEdit(imageAdapter.imageBitmapList)
                }
            }
        }

        private fun imageChangeCounter() {
            binding.imageViewPager.registerOnPageChangeCallback(
                object :
                    ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        updateImageCounter(position)
                    }
                },
            )
        }

        private fun updateImageCounter(counter: Int) =
            with(binding) {
                val itemCount = imageViewPager.adapter?.itemCount ?: 0
                val index = if (itemCount == 0) 0 else 1
                val imageCounter = "${counter + index}/$itemCount"
                imageCounterTextView.text = imageCounter
            }

        private fun showSpinnerPopup(
            textView: TextView,
            items: ArrayList<Pair<String, String>>,
            showSearchBar: Boolean = true,
            onItemSelected: (String) -> Unit,
        ) {
            dialogSpinnerHelper.showDialogSpinner(
                this,
                textView,
                items,
                textView,
                onItemSelectedListener =
                    object : RcViewDialogSpinnerAdapter.OnItemSelectedListener {
                        override fun onItemSelected(item: String) {
                            onItemSelected(item)
                        }
                    },
                showSearchBar,
            )
        }

        private fun setupPublishButton() {
            binding.buttonPublish.setOnClickListener {
                if (areRequiredFieldsEmpty()) {
                    showToast(getString(R.string.edit_required_fields_empty), Toast.LENGTH_SHORT)
                    return@setOnClickListener
                }
                binding.linearLayoutProgress.visibility = View.VISIBLE
                currentAd = createAdFromForm()
                viewModel.viewModelScope.launch {
                    imageManager.uploadImages(
                        currentAd,
                        imageAdapter,
                        IMAGE_UPLOAD_START_INDEX,
                    ) { result ->
                        if (result == true) {
                            showToast(
                                getString(R.string.edit_submitted_for_moderation),
                                Toast.LENGTH_SHORT,
                            )
                        } else {
                            showToast(getString(R.string.edit_submission_error), Toast.LENGTH_SHORT)
                        }
                        binding.linearLayoutProgress.visibility = View.GONE
                        finish()
                    }
                }
            }
        }

        private fun areRequiredFieldsEmpty() =
            with(binding) {
                listOf(
                    selectCountryEditText,
                    selectCityEditText,
                    selectCategoryEditText,
                    adTitleEditText,
                    priceEditText,
                    indexEditText,
                    descriptionEditText,
                    phoneEditText,
                ).any { it.text.isNullOrEmpty() }
            }

        private fun createAdFromForm(): Ad =
            binding.run {
                Ad(
                    currentAd?.key ?: accountManager.generateAdId(),
                    adTitleEditText.text.toString().trim(),
                    adTitleEditText.text
                        .toString()
                        .lowercase()
                        .trim(),
                    selectCountryEditText.text.toString(),
                    selectCityEditText.text.toString(),
                    indexEditText.text.toString().trim(),
                    phoneEditText.text.toString().trim(),
                    sortUtils.getSendOption(selectSendOptionEditText.text.toString()),
                    sortUtils.getCategory(selectCategoryEditText.text.toString()),
                    priceEditText.text
                        .toString()
                        .trim()
                        .toInt(),
                    descriptionEditText.text.toString().trim(),
                    emailEditText.text.toString().trim(),
                    currentAd?.mainImage ?: DEFAULT_IMAGE_URL,
                    currentAd?.image2 ?: DEFAULT_IMAGE_URL,
                    currentAd?.image3 ?: DEFAULT_IMAGE_URL,
                    accountManager.auth.uid,
                    currentAd?.time ?: System.currentTimeMillis().toString(),
                )
            }

        private fun isInEditMode(): Boolean = intent.getBooleanExtra(MainActivity.IS_EDIT_MODE, false)

        private fun populateFields(ad: Ad) =
            with(binding) {
                adTitleEditText.setText(ad.title)
                selectCountryEditText.setText(ad.country)
                selectCityEditText.setText(ad.city)
                indexEditText.setText(ad.index)
                phoneEditText.setText(ad.tel)
                emailEditText.setText(ad.email)
                selectCategoryEditText.setText(ad.category?.let { sortUtils.getCategoryFromSortOption(it) })
                selectSendOptionEditText.setText(ad.withSend?.let { sortUtils.getSendOptionFromSortOption(it) })
                priceEditText.setText(ad.price.toString())
                descriptionEditText.setText(ad.description)
                updateImageCounter(0)
                lifecycleScope.launch {
                    imageManager.fillImageArray(ad, imageAdapter)
                }
            }

        override fun onFragClose(list: ArrayList<Bitmap>) {
            binding.scrollView.visibility = View.VISIBLE
            imageAdapter.update(list)
            updateImageCounter(binding.imageViewPager.currentItem)
        }

        companion object {
            const val IMAGE_UPLOAD_START_INDEX = 0
            const val DEFAULT_IMAGE_URL = ""
            const val EMPTY_STRING = ""
        }

        override fun showImageListFrag(uris: ArrayList<Uri>?) {
            binding.scrollView.visibility = View.GONE
            uris
                ?.takeIf { it.isNotEmpty() }
                ?.let { imageListFragment.resizeAndDisplaySelectedImages(it, true, this) }
            val fm = supportFragmentManager.beginTransaction()
            fm.replace(R.id.edit_ads_fragment_container, imageListFragment)
            fm.commit()
            showStatusBar()
        }

        override fun showToast(
            message: String,
            duration: Int,
        ) {
            Toast.makeText(this, message, duration).show()
        }

        override fun insertAd(
            ad: Ad,
            onResult: (Boolean) -> Unit,
        ) {
            viewModel.viewModelScope.launch {
                onResult(viewModel.insertAd(ad))
            }
        }

        override fun updateImage(
            byteArray: ByteArray,
            oldUrl: String,
            onResult: (Uri) -> Unit,
        ) {
            viewModel.viewModelScope.launch {
                viewModel.updateImage(byteArray, oldUrl)?.let { onResult(it) }
            }
        }

        override fun uploadImage(
            byteArray: ByteArray,
            onResult: (Uri) -> Unit,
        ) {
            viewModel.viewModelScope.launch {
                viewModel.uploadImage(byteArray)?.let { onResult(it) }
            }
        }

        override fun deleteImageByUrl(url: String) {
            viewModel.viewModelScope.launch {
                viewModel.deleteImageByUrl(url)
            }
        }

        override fun getContentResolverAct(): ContentResolver = contentResolver

        override fun addPixToActivityImpl(
            placeholderId: Int,
            options: Options,
            callback: (PixEventCallback.Results) -> Unit,
        ) {
            this.addPixToActivity(placeholderId, options, callback)
        }

        override fun showStatusBarImpl() {
            this.showStatusBar()
        }

        override fun openChooseImageFrag() {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.edit_ads_fragment_container, imageListFragment)
                .commit()
        }

        override fun updateAdapter(uris: ArrayList<Uri>) {
            imageListFragment.updateAdapter(uris, this)
        }

        override fun setSingleImage(uri: Uri) {
            imageListFragment.setOnBindingReadyListener {
                imageListFragment.setSingleImage(uri, selectedImagePosition)
            }
        }

        override fun getTitle(position: Int): String = resources.getStringArray(R.array.title_array)[position]

        override fun getSingleImages(editImagePos: Int) {
            selectedImagePosition = editImagePos
            imageManager.getSingleImages()
        }

        override fun chooseScaleType(bitmap: Bitmap): Boolean = imageManager.chooseScaleType(bitmap)

        override fun onItemDelete() {
            imageListFragment.addImageMenuItem?.isVisible = true
        }
    }
