package com.example.bulletin_board.act

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
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
import com.example.bulletin_board.domain.image.PixImagePickerActions
import com.example.bulletin_board.domain.image.ViewModelHandler
import com.example.bulletin_board.fragments.EditImagePosListener
import com.example.bulletin_board.fragments.FragmentCloseInterface
import com.example.bulletin_board.fragments.ImageListFrag
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
import timber.log.Timber

@AndroidEntryPoint
class EditAdsActivity :
    AppCompatActivity(),
    FragmentCloseInterface,
    ToastHelper,
    ViewModelHandler,
    ContentResolverProvider,
    PixImagePickerActions,
    EditImagePosListener {
    @Inject
    lateinit var chooseImageFrag: ImageListFrag

    lateinit var binding: ActivityEditAdsBinding
    private lateinit var imageAdapter: ImageAdapter
    private var editImagePos = 0
    private var isEditState = false
    private var ad: Ad? = null
    private val viewModel: FirebaseViewModel by viewModels()

    @Inject
    lateinit var accountManager: AccountManager

    @Inject
    lateinit var imageManager: ImageManager

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(ThemeManager.getSelectedTheme(PreferenceManager.getDefaultSharedPreferences(this)))
        super.onCreate(savedInstanceState)
        binding = ActivityEditAdsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        checkEditState()
        onClickSelectCountryCity()
        onClickSelectCategory()
        onClickPublish()
        imageChangeCounter()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkEditState() {
        if (isEditState()) {
            isEditState = true
            ad = intent.getParcelableExtra(MainActivity.ADS_DATA, Ad::class.java)
            ad?.let { fillViews(it) }
        }
    }

    private fun isEditState(): Boolean = intent.getBooleanExtra(MainActivity.EDIT_STATE, false)

    private fun fillViews(ad: Ad) =
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

    private fun init() {
        imageAdapter = ImageAdapter()
        binding.viewPagerImages.adapter = imageAdapter
    }

    private fun onClickSelectCountryCity() {
        binding.textViewSelectCountry.setOnClickListener {
            val listCountry = CityHelper.getAllCountries(this)
            if (binding.textViewSelectCity.text.toString() != "") {
                binding.textViewSelectCity.setText("")
            }
            val onItemSelectedListener =
                object : RcViewDialogSpinnerAdapter.OnItemSelectedListener {
                    override fun onItemSelected(item: String) {
                        binding.textViewSelectCountry.setText(item)
                    }
                }
            DialogSpinnerHelper.showSpinnerPopup(
                this,
                binding.textViewSelectCountry,
                listCountry,
                binding.textViewSelectCountry,
                onItemSelectedListener,
                true,
            )
        }

        binding.textViewSelectCity.setOnClickListener {
            val selectedCountry = binding.textViewSelectCountry.text.toString()
            if (selectedCountry != getString(R.string.select_country)) {
                val listCity = CityHelper.getAllCities(selectedCountry, this)
                val onItemSelectedListener =
                    object : RcViewDialogSpinnerAdapter.OnItemSelectedListener {
                        override fun onItemSelected(item: String) {
                            binding.textViewSelectCity.setText(item)
                        }
                    }
                DialogSpinnerHelper.showSpinnerPopup(
                    this,
                    binding.textViewSelectCity,
                    listCity,
                    binding.textViewSelectCity,
                    onItemSelectedListener,
                    true,
                )
            } else {
                Toast.makeText(this, "No country selected", Toast.LENGTH_LONG).show()
            }
        }

        binding.textViewSelectWithSend.setOnClickListener {
            val listVariant =
                arrayListOf(
                    Pair("Не важно", "empty"),
                    Pair("С отправкой", "empty"),
                    Pair("Без отправки", "empty)"),
                )

            val onItemSelectedListener =
                object : RcViewDialogSpinnerAdapter.OnItemSelectedListener {
                    override fun onItemSelected(item: String) {
                        binding.textViewSelectWithSend.setText(item)
                    }
                }
            DialogSpinnerHelper.showSpinnerPopup(
                this,
                binding.textViewSelectWithSend,
                listVariant,
                binding.textViewSelectWithSend,
                onItemSelectedListener,
                false,
            )
        }

        binding.imageButton.setOnClickListener {
            if (imageAdapter.mainArray.size == 0) {
                Timber.d("click empty")
                imageManager.getMultiImages(3)
            } else {
                showImageListFrag(null)
                chooseImageFrag?.updateAdapterFromEdit(imageAdapter.mainArray)
            }
        }
    }

    private fun onClickPublish() {
        binding.buttonPublish.setOnClickListener {
            if (isFieldsEmpty()) {
                Toast
                    .makeText(
                        this,
                        "Внимание! Все поля должны быть заполнены!",
                        Toast.LENGTH_SHORT,
                    ).show()
                return@setOnClickListener
            }
            binding.progressLayout.visibility = View.VISIBLE
            ad = fillAnnouncement()
            viewModel.viewModelScope.launch {
                imageManager.uploadImages(ad, imageAdapter, 0) {
                    binding.progressLayout.visibility = View.GONE
                    // Toast.makeText(this@EditAdsActivity, "Объявление отправлено на модерацию!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun isFieldsEmpty(): Boolean =
        with(binding) {
            return textViewSelectCountry.text.toString() == getString(R.string.select_country) ||
                textViewSelectCity.text.toString() == getString(R.string.select_city) ||
                textViewSelectCategory.text.toString() == getString(R.string.select_category) ||
                textViewTitle.text?.isEmpty() ?: true ||
                textViewPrice.text?.isEmpty() ?: true ||
                textViewIndex.text?.isEmpty() ?: true ||
                textViewDescription.text?.isEmpty() ?: true ||
                textViewSelectTelNumb.text?.isEmpty() ?: true
        }

    private fun fillAnnouncement(): Ad {
        val adTemp: Ad
        binding.apply {
            adTemp =
                Ad(
                    ad?.key ?: accountManager.generateAdId(),
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
                    ad?.mainImage ?: "empty",
                    ad?.image2 ?: "empty",
                    ad?.image3 ?: "empty",
                    accountManager.auth.uid,
                    ad?.time ?: System.currentTimeMillis().toString(),
                )
        }
        return adTemp
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
                ArrayList<Pair<String, String>>(listCategory.map { Pair(it, "empty") })
            val onItemSelectedListener =
                object : RcViewDialogSpinnerAdapter.OnItemSelectedListener {
                    override fun onItemSelected(item: String) {
                        binding.textViewSelectCategory.setText(item)
                    }
                }
            DialogSpinnerHelper.showSpinnerPopup(
                this,
                binding.textViewSelectCategory,
                pairsCategory,
                binding.textViewSelectCategory,
                onItemSelectedListener,
                false,
            )
        }
    }

    override fun onFragClose(list: ArrayList<Bitmap>) {
        binding.scrollViewMain.visibility = View.VISIBLE
        imageAdapter.update(list)
        updateImageCounter(binding.viewPagerImages.currentItem)
    }

    override fun showImageListFrag(uris: ArrayList<Uri>?) {
        Timber.d("showImageListFrag $uris")
        if (uris != null) chooseImageFrag.resizeSelectedImages(uris, true, this)
        binding.scrollViewMain.visibility = View.GONE
        val fm = supportFragmentManager.beginTransaction()
        fm.replace(R.id.place_holder, chooseImageFrag)
        fm.commit()
        showStatusBar()
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

    private fun updateImageCounter(counter: Int) {
        var index = 1
        val itemCount = binding.viewPagerImages.adapter?.itemCount
        if (itemCount == 0) index = 0
        val imageCounter = "${counter + index}/$itemCount"
        binding.textViewImageCounter.text = imageCounter
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

    override fun getContentResolverAct(): ContentResolver = this.contentResolver

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
        this.supportFragmentManager
            .beginTransaction()
            .replace(R.id.place_holder, this.chooseImageFrag!!)
            .commit()
    }

    override fun updateAdapter(uris: ArrayList<Uri>) {
        this.chooseImageFrag?.updateAdapter(uris, this)
    }

    override fun setSingleImage(uri: Uri) {
        this.chooseImageFrag?.setSingleImage(uri, this.editImagePos)
    }

    override fun updateEditImagePos(pos: Int) {
        editImagePos = pos
    }
}
