package com.example.bulletin_board.act

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.example.bulletin_board.R
import com.example.bulletin_board.adapters.ImageAdapter
import com.example.bulletin_board.model.Announcement
import com.example.bulletin_board.model.DbManager
import com.example.bulletin_board.databinding.ActivityEditAdsBinding
import com.example.bulletin_board.dialogs.DialogSpinnerHelper
import com.example.bulletin_board.fragments.FragmentCloseInterface
import com.example.bulletin_board.fragments.ImageListFrag
import com.example.bulletin_board.utils.CityHelper
import com.example.bulletin_board.utils.ImagePicker
import com.fxn.utility.PermUtil
import java.io.Serializable
import kotlin.collections.ArrayList


class EditAdsActivity : AppCompatActivity(), FragmentCloseInterface {
    var chooseImageFrag: ImageListFrag? = null
    lateinit var binding: ActivityEditAdsBinding
    private val dialog = DialogSpinnerHelper()
    lateinit var imageAdapter: ImageAdapter
    private val dbManager = DbManager()
    var launcherMultiSelectImage: ActivityResultLauncher<Intent>? = null
    var launcherSingleSelectImage: ActivityResultLauncher<Intent>? = null
    var editImagePos = 0
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
        checkBoxWithSend.isChecked = ad.withSend.toBoolean()
        textViewPrice.setText(ad.price)
        textViewDescription.setText(ad.description)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //ImagePicker.getOptions(this, 3, ImagePicker.REQUEST_CODE_GET_IMAGES)
                } else {
                    Toast.makeText(
                        this,
                        "Approve permissions to open Pix ImagePicker",
                        Toast.LENGTH_LONG
                    ).show()
                }
                return
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun init() {
        imageAdapter = ImageAdapter()
        binding.viewPagerImages.adapter = imageAdapter
        launcherMultiSelectImage = ImagePicker.getLauncherForMultiSelectImages(this)
        launcherSingleSelectImage = ImagePicker.getLauncherForSingleImage(this)
//        val listCountry = CityHelper.getAllCountries(this)
//        dialog.showSpinnerDialog(this, listCountry)
    }


    private fun onClickSelectCountryCity() {

        binding.textViewSelectCountry.setOnClickListener {
            val listCountry = CityHelper.getAllCountries(this)
            dialog.showSpinnerDialog(this, listCountry, binding.textViewSelectCountry)
            if (binding.textViewSelectCity.text.toString() != "") {   //getString(R.string.select_city)
                binding.textViewSelectCity.setText("")//getString(R.string.select_city)
            }
        }

        binding.textViewSelectCity.setOnClickListener {
            val selectedCountry = binding.textViewSelectCountry.text.toString()
            if (selectedCountry != getString(R.string.select_country)) {
                val listCity = CityHelper.getAllCities(selectedCountry, this)
                dialog.showSpinnerDialog(this, listCity, binding.textViewSelectCity)
            } else {
                Toast.makeText(this, "No country selected", Toast.LENGTH_LONG).show()
            }
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

                ImagePicker.launcher(this, launcherMultiSelectImage, 3)
                //ImagePicker.getImages(this)
            } else {

                openChooseImageFrag(null)
                chooseImageFrag?.updateAdapterFromEdit(imageAdapter.mainArray)

            }
        }
    }

    fun onClickPublish() {
        binding.buttonPublish.setOnClickListener {
            val adTemp = fillAnnouncement()
            if (isEditState){
                dbManager.publishAnnouncement(adTemp.copy(key = ad?.key), onPublishFinish())
            } else{
                dbManager.publishAnnouncement(adTemp, onPublishFinish())

            }
        }
    }

    private fun onPublishFinish(): DbManager.FinishWorkListener{
        return object: DbManager.FinishWorkListener{
            override fun onFinish() {
                finish()
            }

        }
    }

    private fun fillAnnouncement(): Announcement {
        val announcement: Announcement
        binding.apply {
            announcement = Announcement(
                textViewTitle.text.toString(),
                textViewSelectCountry.text.toString(),
                textViewSelectCity.text.toString(),
                textViewIndex.text.toString(),
                textViewSelectTelNumb.text.toString(),
                checkBoxWithSend.isChecked.toString(),
                textViewSelectCategory.text.toString(),
                textViewPrice.text.toString(),
                textViewDescription.text.toString(),
                dbManager.database.push().key,
                dbManager.auth.uid
            )
        }
        return announcement
    }

    fun onClickSelectCategory() {

        binding.textViewSelectCategory.setOnClickListener {

            val listCategory =
                resources.getStringArray(R.array.category).toMutableList() as ArrayList
            dialog.showSpinnerDialog(this, listCategory, binding.textViewSelectCategory)

        }
    }

    override fun onFragClose(list: ArrayList<Bitmap>) {
        binding.scrollViewMain.visibility = View.VISIBLE
        imageAdapter.update(list)
        chooseImageFrag = null

    }

    fun openChooseImageFrag(newList: ArrayList<String>?) {
        chooseImageFrag = ImageListFrag(this, newList)
        binding.scrollViewMain.visibility = View.GONE
        val fm = supportFragmentManager.beginTransaction()
        fm.replace(R.id.place_holder, chooseImageFrag!!)
        fm.commit()
    }
}