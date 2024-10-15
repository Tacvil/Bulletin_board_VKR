package com.example.bulletin_board.domain.dialog

import android.view.View
import android.widget.TextView
import com.example.bulletin_board.presentation.adapter.RcViewDialogSpinnerAdapter

interface OrderByFilterDialog {
    fun showSpinnerPopup(
        anchorView: View,
        list: ArrayList<Pair<String, String>>,
        tvSelection: TextView,
        onItemSelectedListener: RcViewDialogSpinnerAdapter.OnItemSelectedListener?,
        isSearchable: Boolean,
    )
}
