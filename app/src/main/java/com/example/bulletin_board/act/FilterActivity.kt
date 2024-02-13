package com.example.bulletin_board.act

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.example.bulletin_board.R
import com.example.bulletin_board.databinding.ActivityFilterBinding
import com.example.bulletin_board.dialogs.DialogSpinnerHelper
import com.example.bulletin_board.model.DbManager
import com.example.bulletin_board.utils.CityHelper
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.getField
import java.io.Serializable

class FilterActivity : AppCompatActivity() {
    lateinit var binding: ActivityFilterBinding
    private val dialog = DialogSpinnerHelper()
    private var minPrice: Int? = null
    private var maxPrice: Int? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = FirebaseFirestore.getInstance()
        val collectionReference = db.collection(DbManager.MAIN_NODE)

        val queryMinPrice: Query = collectionReference
            .orderBy("price", Query.Direction.ASCENDING)
            .limit(1)

        val queryMaxPrice: Query = collectionReference
            .orderBy("price", Query.Direction.DESCENDING)
            .limit(1)

        queryMinPrice.get().addOnSuccessListener { minPriceSnapshot ->
            if (!minPriceSnapshot.isEmpty) {
                val minPriceDocument = minPriceSnapshot.documents[0]
                minPrice = minPriceDocument.getField<Int?>("price")?.toInt()

                binding.textViewPriceFromLayout.hint = "от $minPrice"
            }
        }

        queryMaxPrice.get().addOnSuccessListener { maxPriceSnapshot ->
            if (!maxPriceSnapshot.isEmpty) {
                val maxPriceDocument = maxPriceSnapshot.documents[0]
                maxPrice = maxPriceDocument.getField<Int?>("price")?.toInt()

                binding.textViewPriceToLayout.hint = "до $maxPrice"
                focusChangeLstener(minPrice, maxPrice)
            }
        }
        onClickSelectCountryCity()
        onClickDone()
        onClickClear()
        actionBarSettings()
        getFilter()
    }

    private fun focusChangeLstener(minPrice: Int?, maxPrice: Int?) = with(binding) {
        textViewPriceFromLayout.editText?.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            Log.d("sdsdsdwewe", hasFocus.toString())
            if (hasFocus) {
                // Фокус получен, убираем из хинта лишние слова
                textViewPriceFromLayout.hint = "от"
            } else {
                Log.d("minPrice", minPrice.toString())
                if (minPrice != null){
                    textViewPriceFromLayout.hint = "от $minPrice"
                }else{
                    textViewPriceFromLayout.hint = "от"
                }
            }
        }

        textViewPriceToLayout.editText?.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Фокус получен, убираем из хинта лишние слова
                textViewPriceToLayout.hint = "до"
            } else {
                if (maxPrice != null){
                    textViewPriceToLayout.hint = "до $maxPrice"
                }else{
                    textViewPriceToLayout.hint = "до"
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }

    private fun getFilter() = with(binding){
        val filter = intent.getSerializableExtra(FILTER_KEY) as? MutableMap<String, String>
        if (!filter.isNullOrEmpty()){

            if (!filter["keyWords"].isNullOrEmpty()) textViewTitle.setText(filter["keyWords"])
            if (!filter["country"].isNullOrEmpty()) textViewSelectCountry.setText(filter["country"])
            if (!filter["city"].isNullOrEmpty()) textViewSelectCity.setText(filter["city"])
            if (!filter["index"].isNullOrEmpty()) textViewIndex.setText(filter["index"])
            if (!filter["price_from"].isNullOrEmpty()) textViewPriceFrom.setText(filter["price_from"])
            if (!filter["price_to"].isNullOrEmpty()) textViewPriceTo.setText(filter["price_to"])
            if (!filter["withSend"].isNullOrEmpty()) textViewSelectWithSend.setText(filter["withSend"])
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

        textViewSelectWithSend.setOnClickListener{
            val listVariant = arrayListOf("Не важно", "С отправкой", "Без отправки")
            dialog.showSpinnerDialog(this@FilterActivity, listVariant, textViewSelectWithSend)
        }
    }

    private fun onClickDone() = with(binding) {
        buttonDone.setOnClickListener {
            Log.d("MyLogFilterActivity", "Filter: ${createFilter()}")
            val i = Intent().apply {
                putExtra(FILTER_KEY, createFilter() as Serializable)
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

    private fun createFilter(): MutableMap<String, String> = with(binding){
        val sBuilder = StringBuilder()
        val filters = mutableMapOf<String, String>()

        val titleValidate = textViewTitle.text?.split(" ")?.joinToString("-")

        filters["keyWords"] = titleValidate.toString()
        filters["country"] = textViewSelectCountry.text.toString()
        filters["city"] = textViewSelectCity.text.toString()
        filters["index"] = textViewIndex.text.toString()
        filters["withSend"] = textViewSelectWithSend.text.toString()
        filters["price_from"] = textViewPriceFrom.text.toString()
        filters["price_to"] = textViewPriceTo.text.toString()

/*        val arrayTempFilter = listOf(textViewTitle.text,
            textViewSelectCountry.text,
            textViewSelectCity.text,
            textViewIndex.text.toString())
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
        return sBuilder.toString()*/
        return filters
    }

    fun actionBarSettings(){
        val ab = supportActionBar
        ab?.setDisplayHomeAsUpEnabled(true)
    }

    companion object{
        const val FILTER_KEY = "filter_key"
    }
}