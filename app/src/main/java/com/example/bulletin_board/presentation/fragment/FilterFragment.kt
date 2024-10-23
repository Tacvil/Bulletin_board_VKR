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
import com.example.bulletin_board.data.utils.SortUtils
import com.example.bulletin_board.databinding.FragmentFilterBinding
import com.example.bulletin_board.domain.location.CityDataSourceProvider
import com.example.bulletin_board.presentation.adapters.RcViewDialogSpinnerAdapter
import com.example.bulletin_board.presentation.dialogs.DialogSpinnerHelper
import com.example.bulletin_board.presentation.utils.KeyboardUtils
import com.example.bulletin_board.presentation.viewModel.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class FilterFragment
    @Inject
    constructor(
        private val cityDataSourceProvider: CityDataSourceProvider,
        private val dialogSpinnerHelper: DialogSpinnerHelper,
    ) : BottomSheetDialogFragment() {
        private val viewModel: MainViewModel by activityViewModels()
        private var _binding: FragmentFilterBinding? = null
        val binding get() = _binding!!

        @Inject
        lateinit var sortUtils: SortUtils

        private var focusedEditText: TextInputEditText? = null

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
        ): View {
            _binding = FragmentFilterBinding.inflate(inflater, container, false)
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
                        binding.priceFromTextInputLayout.hint = "${getString(R.string.hint_from)} $min"
                        binding.priceToTextInputLayout.hint = "${getString(R.string.hint_to)} $max"
                        focusChangeListener(min, max)
                    }
                }
            }

            val bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

            getFilter()
            setupSelectors()
            onClickApplyFilters()
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
            setHintOnFocusChange(priceFromTextInputLayout, minPrice, getString(R.string.hint_from))
            setHintOnFocusChange(priceToTextInputLayout, maxPrice, getString(R.string.hint_to))
        }

        private fun setHintOnFocusChange(
            textInputLayout: TextInputLayout,
            price: Int?,
            hint: String,
        ) {
            textInputLayout.editText?.onFocusChangeListener =
                View.OnFocusChangeListener { v, hasFocus ->
                    if (hasFocus) focusedEditText = v as? TextInputEditText

                    textInputLayout.hint = hint.takeIf { hasFocus } ?: "$hint ${price ?: 0}"
                }
        }

        private fun getFilter() =
            with(binding) {
                viewModel.getFilterValue(COUNTRY_FIELD)?.let { selectCountryEditText.setText(it) }
                viewModel.getFilterValue(CITY_FIELD)?.let { selectCityEditText.setText(it) }
                viewModel.getFilterValue(INDEX_FIELD)?.let { indexEditText.setText(it) }
                viewModel.getFilterValue(PRICE_FROM_FIELD)?.let { priceFromEditText.setText(it) }
                viewModel.getFilterValue(PRICE_TO_FIELD)?.let { priceToEditText.setText(it) }
                viewModel.getFilterValue(WITH_SEND_FIELD)?.let { selectSendOptionEditText.setText(it) }
            }

        private fun setupSelectors() =
            with(binding) {
                indexEditText.onFocusChangeListener =
                    View.OnFocusChangeListener { v, hasFocus ->
                        if (hasFocus) {
                            focusedEditText = v as? TextInputEditText
                        }
                    }

                selectCountryEditText.setOnClickListener {
                    focusedEditText?.clearFocus()
                    KeyboardUtils.hideKeyboard(requireContext(), selectCountryEditText)
                    if (selectCityEditText.text.toString() != EMPTY_STRING) {
                        selectCityEditText.setText(EMPTY_STRING)
                    }
                    showSpinnerPopup(selectCountryEditText, cityDataSourceProvider.getAllCountries()) {
                        selectCountryEditText.setText(it)
                    }
                }

                selectCityEditText.setOnClickListener {
                    focusedEditText?.clearFocus()
                    KeyboardUtils.hideKeyboard(requireContext(), selectCityEditText)
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
                                requireContext(),
                                getString(R.string.edit_no_country_selected),
                                Toast.LENGTH_SHORT,
                            ).show()
                    }
                }

                selectSendOptionEditText.setOnClickListener {
                    focusedEditText?.clearFocus()
                    KeyboardUtils.hideKeyboard(requireContext(), selectSendOptionEditText)
                    val deliveryOptionsList =
                        arrayListOf(
                            Pair(getString(R.string.no_matter), EMPTY_STRING),
                            Pair(getString(R.string.with_sending), EMPTY_STRING),
                            Pair(getString(R.string.without_sending), EMPTY_STRING),
                        )
                    showSpinnerPopup(selectSendOptionEditText, deliveryOptionsList, false) {
                        selectSendOptionEditText.setText(it)
                    }
                }
            }

        private fun showSpinnerPopup(
            textView: TextView,
            items: ArrayList<Pair<String, String>>,
            showSearchBar: Boolean = true,
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
                showSearchBar,
            )
        }

        private fun onClickApplyFilters() =
            with(binding) {
                applyFilterButton.setOnClickListener {
                    createFilter()
                    dismiss()
                }
            }

        private fun onClickClear() =
            with(binding) {
                clearFilterButton.setOnClickListener {
                    selectCountryEditText.setText(EMPTY_STRING)
                    selectCityEditText.setText(EMPTY_STRING)
                    selectCountryTextInputLayout.hint = getString(R.string.edit_select_country)
                    selectCityTextInputLayout.hint = getString(R.string.edit_select_city)
                    indexEditText.setText(EMPTY_STRING)
                    selectSendOptionEditText.setText(getString(R.string.no_matter))
                    priceFromEditText.setText(EMPTY_STRING)
                    priceToEditText.setText(EMPTY_STRING)
                }
            }

    private fun createFilter() {
        with(binding) {
            val filters = mutableMapOf<String, String>()

            selectCountryEditText.text.toString().takeIf { it.isNotBlank() }?.let {
                filters[COUNTRY_FIELD] = it
            }
            selectCityEditText.text.toString().takeIf { it.isNotBlank() }?.let {
                filters[CITY_FIELD] = it
            }
            indexEditText.text.toString().takeIf { it.isNotBlank() }?.let {
                filters[INDEX_FIELD] = it
            }
            if (selectSendOptionEditText.text.toString() != getString(R.string.no_matter)) {
                filters[WITH_SEND_FIELD] = sortUtils.getSendOption(selectSendOptionEditText.text.toString())
            }
            priceFromEditText.text.toString().takeIf { it.isNotBlank() }?.let {
                filters[PRICE_FROM_FIELD] = it
            }
            priceToEditText.text.toString().takeIf { it.isNotBlank() }?.let {
                filters[PRICE_TO_FIELD] = it
            }

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
