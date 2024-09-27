package com.example.bulletin_board.act

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.example.bulletin_board.R
import com.example.bulletin_board.databinding.ActivityFilterBinding
import com.example.bulletin_board.dialogs.DialogSpinnerHelper
import com.example.bulletin_board.dialogs.RcViewDialogSpinnerAdapter
import com.example.bulletin_board.packroom.RemoteAdDataSource.Companion.CITY_FIELD
import com.example.bulletin_board.packroom.RemoteAdDataSource.Companion.COUNTRY_FIELD
import com.example.bulletin_board.packroom.RemoteAdDataSource.Companion.INDEX_FIELD
import com.example.bulletin_board.packroom.RemoteAdDataSource.Companion.PRICE_FROM_FIELD
import com.example.bulletin_board.packroom.RemoteAdDataSource.Companion.PRICE_TO_FIELD
import com.example.bulletin_board.packroom.RemoteAdDataSource.Companion.WITH_SEND_FIELD
import com.example.bulletin_board.packroom.SortOption
import com.example.bulletin_board.utils.CityHelper
import com.example.bulletin_board.viewmodel.FirebaseViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class FilterFragment : BottomSheetDialogFragment() {
    private var _binding: ActivityFilterBinding? = null
    val binding get() = _binding!!
    private val dialog = DialogSpinnerHelper()
    private lateinit var defPreferences: SharedPreferences
    private val viewModel: FirebaseViewModel by activityViewModels()

    // override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    init {
        Timber.d("FilterFragment init")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = ActivityFilterBinding.inflate(inflater, container, false)
        return binding.root // Возвращаем root из binding
    }

    companion object {
        const val TAG = "ModalBottomSheet"
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        defPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())

        lifecycleScope.launch {
            viewModel.getMinPrice()
            viewModel.getMaxPrice()
            viewModel.appState.collectLatest { appState ->
                val minPrice = appState.minPrice
                val maxPrice = appState.maxPrice

                if (minPrice != null && maxPrice != null) {
                    Timber.d("minPrice: $minPrice, maxPrice: $maxPrice")
                    binding.textViewPriceFromLayout.hint = "от $minPrice"
                    binding.textViewPriceToLayout.hint = "до $maxPrice"
                    focusChangeListener(minPrice, maxPrice)
                }
            }
        }

        val bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        getFilter()
        onClickSelectCountryCity()
        onClickDone()
        onClickClear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun focusChangeListener(
        minPrice: Int?,
        maxPrice: Int?,
    ) = with(binding) {
        val fromHint = getString(R.string.hint_from)
        val toHint = getString(R.string.hint_to)

        setHintOnFocusChange(textViewPriceFromLayout, minPrice, fromHint)
        setHintOnFocusChange(textViewPriceToLayout, maxPrice, toHint)
    }

    private fun setHintOnFocusChange(
        textInputLayout: TextInputLayout,
        price: Int?,
        hint: String,
    ) {
        textInputLayout.editText?.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                textInputLayout.hint = if (hasFocus) hint else "$hint ${price ?: ""}"
            }
    }

    private fun getFilter() =
        with(binding) {
            viewModel.getFilterValue(COUNTRY_FIELD)?.let { textViewSelectCountry.setText(it) }
            viewModel.getFilterValue(CITY_FIELD)?.let { textViewSelectCity.setText(it) }
            viewModel.getFilterValue(INDEX_FIELD)?.let { textViewIndex.setText(it) }
            viewModel.getFilterValue(PRICE_FROM_FIELD)?.let { textViewPriceFrom.setText(it) }
            viewModel.getFilterValue(PRICE_TO_FIELD)?.let { textViewPriceTo.setText(it) }
            viewModel.getFilterValue(WITH_SEND_FIELD)?.let { textViewSelectWithSend.setText(it) }
        }

    private fun onClickSelectCountryCity() =
        with(binding) {
            textViewSelectCountry.setOnClickListener {
                if (textViewSelectCity.text.toString() != "") {
                    textViewSelectCity.setText("")
                }
                showSpinnerPopup(textViewSelectCountry, CityHelper.getAllCountries(requireContext())) {
                    textViewSelectCountry.setText(it)
                }
            }

            textViewSelectCity.setOnClickListener {
                val selectedCountry = textViewSelectCountry.text.toString()
                if (selectedCountry != getString(R.string.select_country)) {
                    showSpinnerPopup(textViewSelectCity, CityHelper.getAllCities(selectedCountry, requireContext())) {
                        textViewSelectCity.setText(it)
                    }
                } else {
                    Toast.makeText(requireContext(), "No country selected", Toast.LENGTH_LONG).show()
                }
            }

            textViewSelectWithSend.setOnClickListener {
                val listVariant =
                    arrayListOf(
                        Pair(getString(R.string.no_matter), ""),
                        Pair(getString(R.string.with_sending), ""),
                        Pair(getString(R.string.without_sending), ""),
                    )
                showSpinnerPopup(textViewSelectWithSend, listVariant, false) {
                    textViewSelectWithSend.setText(it)
                }
            }
        }

    private fun showSpinnerPopup(
        textView: TextView,
        items: ArrayList<Pair<String, String>>,
        isCountry: Boolean = true,
        onItemSelected: (String) -> Unit,
    ) {
        dialog.showSpinnerPopup(
            requireContext(),
            textView,
            items,
            textView,
            object : RcViewDialogSpinnerAdapter.OnItemSelectedListener {
                override fun onItemSelected(item: String) {
                    onItemSelected(item)
                }
            },
            isCountry,
        )
    }

    private fun onClickDone() =
        with(binding) {
            buttonDone.setOnClickListener {
                createFilter()
                Timber.d("Filter: " + viewModel.appState.value.filter)
                dismiss() // Закрыть Bottom Sheet
            }
        }

    private fun onClickClear() =
        with(binding) {
            buttonClearFilter.setOnClickListener {
                textViewSelectCountry.setText("")
                textViewSelectCity.setText("")
                textViewSelectCountryLayout.hint = getString(R.string.select_country)
                textViewSelectCityLayout.hint = getString(R.string.select_city)
                textViewIndex.setText("")
                textViewSelectWithSend.setText("Не важно")
            }
        }

    private fun createFilter() {
        with(binding) {
            val filters = mutableMapOf<String, String>()
            filters[COUNTRY_FIELD] = textViewSelectCountry.text.toString()
            filters[CITY_FIELD] = textViewSelectCity.text.toString()
            filters[INDEX_FIELD] = textViewIndex.text.toString()
            if (textViewSelectWithSend.text.toString() != getString(R.string.no_matter)) {
                filters[WITH_SEND_FIELD] =
                    when (textViewSelectWithSend.text.toString()) {
                        getString(R.string.with_sending) -> SortOption.WITH_SEND.id
                        getString(R.string.without_sending) -> SortOption.WITHOUT_SEND.id
                        else -> ""
                    }
            }
            filters[PRICE_FROM_FIELD] = textViewPriceFrom.text.toString()
            filters[PRICE_TO_FIELD] = textViewPriceTo.text.toString()
            viewModel.updateFilters(filters)
        }
    }
}
