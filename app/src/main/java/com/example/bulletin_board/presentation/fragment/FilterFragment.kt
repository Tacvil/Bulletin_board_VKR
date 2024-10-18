package com.example.bulletin_board.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.bulletin_board.R
import com.example.bulletin_board.data.datasource.RemoteAdDataSource.Companion.CITY_FIELD
import com.example.bulletin_board.data.datasource.RemoteAdDataSource.Companion.COUNTRY_FIELD
import com.example.bulletin_board.data.datasource.RemoteAdDataSource.Companion.INDEX_FIELD
import com.example.bulletin_board.data.datasource.RemoteAdDataSource.Companion.ORDER_BY_FIELD
import com.example.bulletin_board.data.datasource.RemoteAdDataSource.Companion.PRICE_FROM_FIELD
import com.example.bulletin_board.data.datasource.RemoteAdDataSource.Companion.PRICE_TO_FIELD
import com.example.bulletin_board.data.datasource.RemoteAdDataSource.Companion.WITH_SEND_FIELD
import com.example.bulletin_board.data.utils.SortOption
import com.example.bulletin_board.databinding.ActivityFilterBinding
import com.example.bulletin_board.domain.location.CityDataSourceProvider
import com.example.bulletin_board.presentation.adapter.RcViewDialogSpinnerAdapter
import com.example.bulletin_board.presentation.dialogs.DialogSpinnerHelper
import com.example.bulletin_board.presentation.viewModel.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FilterFragment
    @Inject
    constructor(
        private val cityDataSourceProvider: CityDataSourceProvider,
        private val dialogSpinnerHelper: DialogSpinnerHelper,
    ) : BottomSheetDialogFragment() {
        private val viewModel: MainViewModel by activityViewModels()
        private var _binding: ActivityFilterBinding? = null
        val binding get() = _binding!!

        // override fun getTheme(): Int = R.style.BottomSheetDialogTheme

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
        ): View {
            _binding = ActivityFilterBinding.inflate(inflater, container, false)
            return binding.root
        }

        override fun onViewCreated(
            view: View,
            savedInstanceState: Bundle?,
        ) {
            super.onViewCreated(view, savedInstanceState)

            lifecycleScope.launch {
                viewModel.getMinMaxPrice()
                viewModel.appState.collectLatest { appState ->
                    appState.minMaxPrice?.let { (min, max) ->
                        binding.textViewPriceFromLayout.hint = "${getString(R.string.hint_from)} $min"
                        binding.textViewPriceToLayout.hint = "${getString(R.string.hint_to)} $max"
                        focusChangeListener(min, max)
                    }
                }
            }

            val bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

            getFilter()
            setupSelectors()
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
            setHintOnFocusChange(textViewPriceFromLayout, minPrice, getString(R.string.hint_from))
            setHintOnFocusChange(textViewPriceToLayout, maxPrice, getString(R.string.hint_to))
        }

        private fun setHintOnFocusChange(
            textInputLayout: TextInputLayout,
            price: Int?,
            hint: String,
        ) {
            textInputLayout.editText?.onFocusChangeListener =
                View.OnFocusChangeListener { _, hasFocus ->
                    textInputLayout.hint =
                        if (hasFocus) {
                            hint
                        } else {
                            "$hint ${price ?: 0}"
                        }
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

        private fun setupSelectors() =
            with(binding) {
                textViewSelectCountry.setOnClickListener {
                    if (textViewSelectCity.text.toString() != EMPTY_STRING) {
                        textViewSelectCity.setText(EMPTY_STRING)
                    }
                    showSpinnerPopup(textViewSelectCountry, cityDataSourceProvider.getAllCountries()) {
                        textViewSelectCountry.setText(it)
                    }
                }

                textViewSelectCity.setOnClickListener {
                    val selectedCountry = textViewSelectCountry.text.toString()
                    if (selectedCountry != getString(R.string.select_country)) {
                        showSpinnerPopup(textViewSelectCity, cityDataSourceProvider.getAllCities(selectedCountry)) {
                            textViewSelectCity.setText(it)
                        }
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.no_country_selected), Toast.LENGTH_LONG).show()
                    }
                }

                textViewSelectWithSend.setOnClickListener {
                    val listVariant =
                        arrayListOf(
                            Pair(getString(R.string.no_matter), EMPTY_STRING),
                            Pair(getString(R.string.with_sending), EMPTY_STRING),
                            Pair(getString(R.string.without_sending), EMPTY_STRING),
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
            dialogSpinnerHelper.showDialogSpinner(
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
                    dismiss()
                }
            }

        private fun onClickClear() =
            with(binding) {
                buttonClearFilter.setOnClickListener {
                    textViewSelectCountry.setText(EMPTY_STRING)
                    textViewSelectCity.setText(EMPTY_STRING)
                    textViewSelectCountryLayout.hint = getString(R.string.select_country)
                    textViewSelectCityLayout.hint = getString(R.string.select_city)
                    textViewIndex.setText(EMPTY_STRING)
                    textViewSelectWithSend.setText(getString(R.string.no_matter))
                    textViewPriceFrom.setText(EMPTY_STRING)
                    textViewPriceTo.setText(EMPTY_STRING)
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
                            else -> EMPTY_STRING
                        }
                }
                filters[PRICE_FROM_FIELD] = textViewPriceFrom.text.toString()
                filters[PRICE_TO_FIELD] = textViewPriceTo.text.toString()

                if (filters[PRICE_FROM_FIELD]?.isNotEmpty() == true || filters[PRICE_TO_FIELD]?.isNotEmpty() == true) {
                    filters[ORDER_BY_FIELD] = SortOption.BY_PRICE_ASC.id
                }

                viewModel.updateFilters(filters)
            }
        }

        companion object {
            const val FILTER_FRAGMENT_TAG = "FilterFragment"
            const val EMPTY_STRING = ""
        }
    }
