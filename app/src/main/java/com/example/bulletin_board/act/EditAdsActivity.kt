package com.example.bulletin_board.act

import android.content.ContentResolver
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
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import com.example.bulletin_board.R
import com.example.bulletin_board.adapters.ImageAdapter
import com.example.bulletin_board.databinding.ActivityEditAdsBinding
import com.example.bulletin_board.dialogs.DialogSpinnerHelper
import com.example.bulletin_board.dialogs.RcViewDialogSpinnerAdapter
import com.example.bulletin_board.domain.AccountManager
import com.example.bulletin_board.domain.ThemeManager
import com.example.bulletin_board.domain.ToastHelper
import com.example.bulletin_board.domain.image.ContentResolverProvider
import com.example.bulletin_board.domain.image.ImageManager
import com.example.bulletin_board.domain.image.PixImagePicker.Companion.MAX_IMAGE_COUNT
import com.example.bulletin_board.domain.image.PixImagePickerActions
import com.example.bulletin_board.domain.image.ViewModelHandler
import com.example.bulletin_board.fragments.FragmentCloseInterface
import com.example.bulletin_board.fragments.ImageAdapterHandler
import com.example.bulletin_board.fragments.ImageListFragment
import com.example.bulletin_board.fragments.OnItemDeleteListener
import com.example.bulletin_board.model.Ad
import com.example.bulletin_board.pix.models.Options
import com.example.bulletin_board.utils.CityHelper
import com.example.bulletin_board.viewmodel.FirebaseViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.ak1.pix.helpers.PixEventCallback
import io.ak1.pix.helpers.addPixToActivity
import io.ak1.pix.helpers.showStatusBar
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditAdsActivity :
    AppCompatActivity(),
    FragmentCloseInterface,
    ToastHelper,
    ViewModelHandler,
    ContentResolverProvider,
    PixImagePickerActions,
    OnItemDeleteListener,
    ImageAdapterHandler {
    private val viewModel: FirebaseViewModel by viewModels()
    private lateinit var binding: ActivityEditAdsBinding

    @Inject lateinit var imageListFragment: ImageListFragment

    @Inject lateinit var accountManager: AccountManager

    @Inject lateinit var imageManager: ImageManager

    @Inject lateinit var imageAdapter: ImageAdapter

    private var selectedImagePosition = 0
    private var isInEditMode = false
    private var currentAd: Ad? = null

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
        onClickSelectCategory()
        setupPublishButton()
        imageChangeCounter()
    }

    private fun initializeComponents() {
        binding.viewPagerImages.adapter = imageAdapter
    }

    private fun setupSelectors() {
        with(binding) {
            textViewSelectCountry.setOnClickListener {
                val countryList = CityHelper.getAllCountries(this@EditAdsActivity)
                textViewSelectCity.setText("")
                showSelectDialog(textViewSelectCountry, countryList, true) { item ->
                    textViewSelectCountry.setText(item)
                }
            }

            textViewSelectCity.setOnClickListener {
                val selectedCountry = textViewSelectCountry.text.toString()
                if (selectedCountry != getString(R.string.select_country)) {
                    val cityList = CityHelper.getAllCities(selectedCountry, this@EditAdsActivity)
                    showSelectDialog(textViewSelectCity, cityList, true) { item ->
                        textViewSelectCity.setText(item)
                    }
                } else {
                    showToast(getString(R.string.no_country_selected), Toast.LENGTH_LONG)
                }
            }

            textViewSelectWithSend.setOnClickListener {
                val deliveryOptionsList =
                    arrayListOf(
                        Pair(getString(R.string.no_matter), EMPTY_STRING),
                        Pair(getString(R.string.with_sending), EMPTY_STRING),
                        Pair(getString(R.string.without_sending), EMPTY_STRING),
                    )
                showSelectDialog(textViewSelectWithSend, deliveryOptionsList, false) { item ->
                    textViewSelectWithSend.setText(item)
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
        binding.viewPagerImages.registerOnPageChangeCallback(
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
            val itemCount = viewPagerImages.adapter?.itemCount ?: 0
            val index = if (itemCount == 0) 0 else 1
            val imageCounter = "${counter + index}/$itemCount"
            textViewImageCounter.text = imageCounter
        }

    private fun showSelectDialog(
        textView: TextView,
        items: ArrayList<Pair<String, String>>,
        isCountryCity: Boolean,
        onItemSelected: (String) -> Unit,
    ) {
        DialogSpinnerHelper.showDialogSpinner(
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
            isCountryCity,
        )
    }

    private fun setupPublishButton() {
        binding.buttonPublish.setOnClickListener {
            if (areRequiredFieldsEmpty()) {
                showToast(getString(R.string.required_fields_empty), Toast.LENGTH_SHORT)
                return@setOnClickListener
            }
            binding.progressLayout.visibility = View.VISIBLE
            currentAd = createAdFromForm()
            viewModel.viewModelScope.launch {
                imageManager.uploadImages(currentAd, imageAdapter, IMAGE_UPLOAD_START_INDEX) {
                    binding.progressLayout.visibility = View.GONE
                    finish()
                }
            }
        }
    }

    private fun areRequiredFieldsEmpty() =
        with(binding) {
            listOf(
                textViewSelectCountry,
                textViewSelectCity,
                textViewSelectCategory,
                textViewTitle,
                textViewPrice,
                textViewIndex,
                textViewDescription,
                textViewSelectTelNumb,
            ).any { it.text.isNullOrEmpty() }
        }

    private fun createAdFromForm(): Ad =
        binding.run {
            Ad(
                currentAd?.key ?: accountManager.generateAdId(),
                textViewTitle.text.toString(),
                textViewTitle.text.toString().lowercase(),
                createKeyWords(textViewTitle.text.toString()),
                textViewSelectCountry.text.toString(),
                textViewSelectCity.text.toString(),
                textViewIndex.text.toString(),
                textViewSelectTelNumb.text.toString(),
                textViewSelectWithSend.text.toString(),
                textViewSelectCategory.text.toString(),
                textViewPrice.text.toString().toInt(),
                textViewDescription.text.toString(),
                textViewSelectEmail.text.toString(),
                currentAd?.mainImage ?: DEFAULT_IMAGE_URL,
                currentAd?.image2 ?: DEFAULT_IMAGE_URL,
                currentAd?.image3 ?: DEFAULT_IMAGE_URL,
                accountManager.auth.uid,
                currentAd?.time ?: System.currentTimeMillis().toString(),
            )
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

    private fun isInEditMode(): Boolean = intent.getBooleanExtra(MainActivity.IS_EDIT_MODE, false)

    private fun populateFields(ad: Ad) =
        with(binding) {
            textViewTitle.setText(ad.title)
            textViewSelectCountry.setText(ad.country)
            textViewSelectCity.setText(ad.city)
            textViewIndex.setText(ad.index)
            textViewSelectTelNumb.setText(ad.tel)
            textViewSelectEmail.setText(ad.email)
            textViewSelectCategory.setText(ad.category)
            textViewSelectWithSend.setText(ad.withSend)
            textViewPrice.setText(ad.price.toString())
            textViewDescription.setText(ad.description)
            updateImageCounter(0)
            imageManager.fillImageArray(ad, imageAdapter)
        }

    private fun createKeyWords(title: String): ArrayList<String> {
        val words = title.split(" ")
        val combinations = generateCombinations(words)
        return combinations as ArrayList<String>
    }

    private fun generateCombinations(
        words: List<String>,
        memo: MutableMap<List<String>, List<String>> = mutableMapOf(),
    ): List<String> {
        if (words.isEmpty()) {
            return emptyList()
        }

        if (memo.containsKey(words)) {
            return memo[words]!!
        }

        val result = mutableListOf<String>()

        for ((index, currentWord) in words.withIndex()) {
            val remainingWords = words.toMutableList().apply { removeAt(index) }
            val subCombinations = generateCombinations(remainingWords, memo)

            result.add(currentWord)
            result.addAll(subCombinations.map { "$currentWord-$it" })
        }

        memo[words] = result
        return result
    }

    private fun onClickSelectCategory() {
        binding.textViewSelectCategory.setOnClickListener {
            val listCategory = resources.getStringArray(R.array.category)
            val pairsCategory =
                ArrayList<Pair<String, String>>(listCategory.map { Pair(it, EMPTY_STRING) })
            showSelectDialog(binding.textViewSelectCategory, pairsCategory, false) { item ->
                binding.textViewSelectCategory.setText(item)
            }
        }
    }

    override fun onFragClose(list: ArrayList<Bitmap>) {
        binding.scrollViewMain.visibility = View.VISIBLE
        imageAdapter.update(list)
        updateImageCounter(binding.viewPagerImages.currentItem)
    }

    companion object {
        const val IMAGE_UPLOAD_START_INDEX = 0
        const val DEFAULT_IMAGE_URL = ""
        const val EMPTY_STRING = ""
    }

    override fun showImageListFrag(uris: ArrayList<Uri>?) {
        uris
            ?.takeIf { it.isNotEmpty() }
            ?.let { imageListFragment.resizeAndDisplaySelectedImages(it, true, this) }
        binding.scrollViewMain.visibility = View.GONE
        val fm = supportFragmentManager.beginTransaction()
        fm.replace(R.id.place_holder, imageListFragment)
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
            .replace(R.id.place_holder, imageListFragment)
            .commit()
    }

    override fun updateAdapter(uris: ArrayList<Uri>) {
        this.imageListFragment.updateAdapter(uris, this)
    }

    override fun setSingleImage(uri: Uri) {
        imageListFragment.setSingleImage(uri, selectedImagePosition)
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
