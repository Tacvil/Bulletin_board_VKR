package com.example.bulletin_board.act

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import com.example.bulletin_board.R
import com.example.bulletin_board.databinding.ActivityFilterBinding
import com.example.bulletin_board.dialogs.DialogSpinnerHelper
import com.example.bulletin_board.utils.CityHelper

class FilterActivity : AppCompatActivity() {
    lateinit var binding: ActivityFilterBinding
    private val dialog = DialogSpinnerHelper()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onClickSelectCountryCity()
        onClickDone()
        onClickClear()
        actionBarSettings()
        getFilter()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }

    private fun getFilter() = with(binding){
        val filter = intent.getStringExtra(FILTER_KEY)
        if (filter != null && filter != "empty"){
            val filterArray = filter.split("_")
            if (filterArray[0] != "empty") textViewSelectCountry.setText(filterArray[0])
            if (filterArray[1] != "empty") textViewSelectCity.setText(filterArray[1])
            if (filterArray[2] != "empty") textViewIndex.setText(filterArray[2])
            checkBoxWithSend.isChecked = filterArray[3].toBoolean()
        }
    }

    private fun onClickSelectCountryCity() = with(binding) {

        textViewSelectCountry.setOnClickListener {
            val listCountry = CityHelper.getAllCountries(this@FilterActivity)
            dialog.showSpinnerDialog(this@FilterActivity, listCountry, textViewSelectCountry)
            if (textViewSelectCity.text.toString() != "") {   //getString(R.string.select_city)
                textViewSelectCity.setText("")//getString(R.string.select_city)
            }
        }

        textViewSelectCity.setOnClickListener {
            val selectedCountry = textViewSelectCountry.text.toString()
            if (selectedCountry != getString(R.string.select_country)) {
                val listCity = CityHelper.getAllCities(selectedCountry, this@FilterActivity)
                dialog.showSpinnerDialog(this@FilterActivity, listCity, textViewSelectCity)
            } else {
                Toast.makeText(this@FilterActivity, "No country selected", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun onClickDone() = with(binding) {
        buttonDone.setOnClickListener {
            Log.d("MyLogFilterActivity", "Filter: ${createFilter()}")
            val i = Intent().apply {
                putExtra(FILTER_KEY, createFilter())
            }
            setResult(RESULT_OK, i)
            finish()
        }
    }

    private fun onClickClear() = with(binding) {
        buttonClearFilter.setOnClickListener {
            textViewSelectCountry.setText(getString(R.string.select_country))
            textViewSelectCity.setText(getString(R.string.select_city))
            textViewIndex.setText("")
            checkBoxWithSend.isChecked = false
            setResult(RESULT_CANCELED)
        }
    }

    private fun createFilter(): String = with(binding){
        val sBuilder = StringBuilder()
        val arrayTempFilter = listOf(textViewSelectCountry.text,
            textViewSelectCity.text,
            textViewIndex.text,
            checkBoxWithSend.isChecked.toString())
        for ((i, s) in arrayTempFilter.withIndex()){
            if (s != null) {
                if (s != getString(R.string.select_country) && s!= getString(R.string.select_city) && s.isNotEmpty()){
                    sBuilder.append(s)
                    if (i != arrayTempFilter.size - 1) sBuilder.append("_")
                }else{
                    sBuilder.append("empty")
                    if (i != arrayTempFilter.size - 1) sBuilder.append("_")
                }

            }
        }
        return sBuilder.toString()
    }

    fun actionBarSettings(){
        val ab = supportActionBar
        ab?.setDisplayHomeAsUpEnabled(true)
    }

    companion object{
        const val FILTER_KEY = "filter_key"
    }
}