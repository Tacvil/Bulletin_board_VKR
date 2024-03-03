package com.example.bulletin_board.act

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.bulletin_board.R
import com.example.bulletin_board.adapters.ImageAdapter
import com.example.bulletin_board.model.Announcement
import com.example.bulletin_board.model.DbManager
import com.example.bulletin_board.databinding.ActivityEditAdsBinding
import com.example.bulletin_board.dialogs.DialogSpinnerHelper
import com.example.bulletin_board.dialogs.RcViewDialogSpinnerAdapter
import com.example.bulletin_board.fragments.FragmentCloseInterface
import com.example.bulletin_board.fragments.ImageListFrag
import com.example.bulletin_board.utils.CityHelper
import com.example.bulletin_board.utils.ImageManager.fillImageArray
import com.example.bulletin_board.utils.ImagePicker
import com.google.android.gms.tasks.OnCompleteListener
import io.ak1.pix.helpers.showStatusBar
import java.io.ByteArrayOutputStream
import java.io.Serializable
import kotlin.collections.ArrayList


class EditAdsActivity : AppCompatActivity(), FragmentCloseInterface {
    var chooseImageFrag: ImageListFrag? = null
    lateinit var binding: ActivityEditAdsBinding
    private val dialog = DialogSpinnerHelper()
    lateinit var imageAdapter: ImageAdapter
    private val dbManager = DbManager()
    var editImagePos = 0
    private var imageIndex = 0
    private var isEditState = false
    private var ad: Announcement? = null
    override fun onCreate(savedInstanceState: Bundle?) {
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

    private fun checkEditState() {
        if (isEditState()) {
            isEditState = true
            ad = intent.serializable<Announcement>(MainActivity.ADS_DATA)
            ad?.let { fillViews(it) }
        }
    }

    private inline fun <reified T : Serializable> Intent.serializable(key: String): T? = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getSerializableExtra(
            key,
            T::class.java
        )

        else -> @Suppress("DEPRECATION") getSerializableExtra(key) as? T
    }

    private fun isEditState(): Boolean {
        return intent.getBooleanExtra(MainActivity.EDIT_STATE, false)
    }

    private fun fillViews(ad: Announcement) = with(binding) {
        textViewTitle.setText(ad.title)
        textViewSelectCountry.setText(ad.country)
        textViewSelectCity.setText(ad.city)
        textViewIndex.setText(ad.index)
        textViewSelectTelNumb.setText(ad.tel)
        textViewSelectCategory.setText(ad.category)
        //checkBoxWithSend.isChecked = ad.withSend.toBoolean()
        textViewSelectWithSend.setText(ad.withSend)
        textViewPrice.setText(ad.price.toString())
        textViewDescription.setText(ad.description)
        updateImageCounter(0)
        fillImageArray(ad, imageAdapter)
    }

    private fun init() {
        imageAdapter = ImageAdapter()
        binding.viewPagerImages.adapter = imageAdapter

//        val listCountry = CityHelper.getAllCountries(this)
//        dialog.showSpinnerDialog(this, listCountry)
    }


    private fun onClickSelectCountryCity() {

        binding.textViewSelectCountry.setOnClickListener {
            val listCountry = CityHelper.getAllCountries(this)
            if (binding.textViewSelectCity.text.toString() != "") {   //getString(R.string.select_city)
                binding.textViewSelectCity.setText("")//getString(R.string.select_city)
            }
            val onItemSelectedListener =
                object : RcViewDialogSpinnerAdapter.OnItemSelectedListener {
                    override fun onItemSelected(item: String) {
                        binding.textViewSelectCountry.setText(item)
                    }
                }
            dialog.showSpinnerPopup(
                this,
                binding.textViewSelectCountry,
                listCountry,
                binding.textViewSelectCountry,
                onItemSelectedListener,
                true
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
                dialog.showSpinnerPopup(
                    this,
                    binding.textViewSelectCity,
                    listCity,
                    binding.textViewSelectCity,
                    onItemSelectedListener,
                    true
                )
            } else {
                Toast.makeText(this, "No country selected", Toast.LENGTH_LONG).show()
            }
        }

        binding.textViewSelectWithSend.setOnClickListener{

            val listVariant = arrayListOf(
                Pair("Не важно", "empty"),
                Pair("С отправкой", "empty"),
                Pair("Без отправки", "empty)"))

            val onItemSelectedListener =
                object : RcViewDialogSpinnerAdapter.OnItemSelectedListener {
                    override fun onItemSelected(item: String) {
                        binding.textViewSelectWithSend.setText(item)
                    }
                }
            dialog.showSpinnerPopup(this, binding.textViewSelectWithSend, listVariant, binding.textViewSelectWithSend, onItemSelectedListener, false )
        }

//        binding.textViewSelectCity.setOnClickListener {
//            val selectedCountry = binding.textViewSelectCountry.text.toString()
//            if (selectedCountry != getString(R.string.select_country)) {
//                val listCity = CityHelper.getAllCities(selectedCountry, this)
//                dialog.showSpinnerDialog(this, listCity, binding.textViewSelectCity)
//            } else {
//                Toast.makeText(this, "No country selected", Toast.LENGTH_LONG).show()
//            }
//        }

        binding.imageButton.setOnClickListener {

            if (imageAdapter.mainArray.size == 0) {

                ImagePicker.getMultiImages(this, 3)
                //ImagePicker.getImages(this)
            } else {

                openChooseImageFrag(null)
                chooseImageFrag?.updateAdapterFromEdit(imageAdapter.mainArray)

            }
        }
    }

    fun onClickPublish() {
        binding.buttonPublish.setOnClickListener {
            if (isFieldsEmpty()) {
                Toast.makeText(
                    this,
                    "Внимание! Все поля должны быть заполнены!",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            binding.progressLayout.visibility = View.VISIBLE
            ad = fillAnnouncement()
            uploadImages()
        }
    }

    private fun isFieldsEmpty(): Boolean = with(binding) {
        return textViewSelectCountry.text.toString() == getString(R.string.select_country) ||
                textViewSelectCity.text.toString() == getString(R.string.select_city) ||
                textViewSelectCategory.text.toString() == getString(R.string.select_category) ||
                textViewTitle.text?.isEmpty() ?: true ||
                textViewPrice.text?.isEmpty() ?: true ||
                textViewIndex.text?.isEmpty() ?: true ||
                textViewDescription.text?.isEmpty() ?: true ||
                textViewSelectTelNumb.text?.isEmpty() ?: true

    }

    private fun onPublishFinish(): DbManager.FinishWorkListener {
        return object : DbManager.FinishWorkListener {
            override fun onFinish(isDone: Boolean) {
                binding.progressLayout.visibility = View.GONE
                if (isDone) finish()
            }

        }
    }

    private fun fillAnnouncement(): Announcement {
        val announcementTemp: Announcement
        binding.apply {
            announcementTemp = Announcement(
                textViewTitle.text.toString(),
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
                ad?.key ?: dbManager.database.push().key,
                dbManager.auth.uid,
                ad?.time ?: System.currentTimeMillis().toString()
            )
        }
        return announcementTemp
    }

    private fun createKeyWords(title: String): ArrayList<String> {
        Log.d("EdAdsAct title", title)
        val words = title.split(" ")
        Log.d("EdAdsAct words", "$words")
        val combinations = generateCombinations(words)
        Log.d("EdAdsAct combinations ", "$combinations")
        return combinations as ArrayList<String>
    }

    private fun generateCombinations(
        words: List<String>,
        memo: MutableMap<List<String>, List<String>> = mutableMapOf()
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

    fun onClickSelectCategory() {

        binding.textViewSelectCategory.setOnClickListener {

            val listCategory = resources.getStringArray(R.array.category)
            val pairsCategory = ArrayList<Pair<String, String>>(listCategory.map { Pair(it, "empty") })
            val onItemSelectedListener = object : RcViewDialogSpinnerAdapter.OnItemSelectedListener {
                    override fun onItemSelected(item: String) {
                        binding.textViewSelectCategory.setText(item)
                    }
                }
            dialog.showSpinnerPopup(
                this,
                binding.textViewSelectCategory,
                pairsCategory,
                binding.textViewSelectCategory,
                onItemSelectedListener,
                false
            )

        }
    }

    override fun onFragClose(list: ArrayList<Bitmap>) {
        binding.scrollViewMain.visibility = View.VISIBLE
        imageAdapter.update(list)
        chooseImageFrag = null
        updateImageCounter(binding.viewPagerImages.currentItem)

    }

    fun openChooseImageFrag(newList: ArrayList<Uri>?) {
        chooseImageFrag = ImageListFrag(this)
        if (newList != null) chooseImageFrag?.resizeSelectedImages(newList, true, this)
        binding.scrollViewMain.visibility = View.GONE
        val fm = supportFragmentManager.beginTransaction()
        fm.replace(R.id.place_holder, chooseImageFrag!!)
        fm.commit()
        showStatusBar()
    }

    private fun uploadImages() {
        //Log.d("index", "$imageIndex")
        if (imageIndex == 3) {
            //dbManager.publishAnnouncement(ad!!, onPublishFinish())
            dbManager.publishAnnouncement1(ad!!, onPublishFinish())
            return
        }
        val oldUrl = getUrlFromAd()
        if (imageAdapter.mainArray.size > imageIndex) {
            val byteArray = prepareImageByteArray(imageAdapter.mainArray[imageIndex])
            if (oldUrl.startsWith("http")) {
                updateImage(byteArray, oldUrl) {
                    nextImage(it.result.toString())
                }
            } else {
                uploadImage(byteArray) {
                    //dbManager.publishAnnouncement(ad!!, onPublishFinish())
                    nextImage(it.result.toString())
                }
            }
        } else {
            if (oldUrl.startsWith("http")) {
                deleteImageByUrl(oldUrl) {
                    nextImage("empty")
                }
            } else {
                nextImage("empty")
            }
        }
    }

    private fun nextImage(uri: String) {
        setImageUriToAd(uri)
        imageIndex++
        uploadImages()
    }

    private fun setImageUriToAd(uri: String) {
        when (imageIndex) {
            0 -> ad = ad?.copy(mainImage = uri)
            1 -> ad = ad?.copy(image2 = uri)
            2 -> ad = ad?.copy(image3 = uri)
        }
    }

    private fun getUrlFromAd(): String {
        return listOf(ad?.mainImage!!, ad?.image2!!, ad?.image3!!)[imageIndex]
    }

    private fun prepareImageByteArray(bitmap: Bitmap): ByteArray {
        val outStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, outStream)
        return outStream.toByteArray()
    }

    private fun uploadImage(byteArray: ByteArray, listener: OnCompleteListener<Uri>) {
        val imStorageRef = dbManager.dbStorage
            .child(dbManager.auth.uid!!)
            .child("image_${System.currentTimeMillis()}")
        val upTask = imStorageRef.putBytes(byteArray)
        upTask.continueWithTask { task ->
            imStorageRef.downloadUrl
        }.addOnCompleteListener(listener)

    }

    private fun updateImage(byteArray: ByteArray, url: String, listener: OnCompleteListener<Uri>) {
        val imStorageRef = dbManager.dbStorage.storage.getReferenceFromUrl(url)
        val upTask = imStorageRef.putBytes(byteArray)
        upTask.continueWithTask { task ->
            imStorageRef.downloadUrl
        }.addOnCompleteListener(listener)

    }

    private fun deleteImageByUrl(oldUrl: String, listener: OnCompleteListener<Void>) {
        dbManager.dbStorage.storage
            .getReferenceFromUrl(oldUrl).delete().addOnCompleteListener(listener)
    }

    private fun imageChangeCounter() {
        binding.viewPagerImages.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateImageCounter(position)
            }
        })
    }

    private fun updateImageCounter(counter: Int) {
        var index = 1
        val itemCount = binding.viewPagerImages.adapter?.itemCount
        if (itemCount == 0) index = 0
        val imageCounter = "${counter + index}/$itemCount"
        binding.textViewImageCounter.text = imageCounter
    }
}