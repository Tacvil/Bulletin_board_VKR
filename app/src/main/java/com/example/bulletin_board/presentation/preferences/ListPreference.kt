package com.example.bulletin_board.presentation.preferences

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import com.example.bulletin_board.R
import com.example.bulletin_board.presentation.activities.SettingsActivity.Companion.LANGUAGE_EN
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ListPreference(
    context: Context,
    attrs: AttributeSet?,
) : Preference(context, attrs) {
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private lateinit var entryValues: Array<String>
    private lateinit var entries: Array<String>

    init {
        context.obtainStyledAttributes(attrs, R.styleable.CustomListPreference).apply {
            val valuesArrayResId = getResourceId(R.styleable.CustomListPreference_android_entryValues, 0)
            val entriesArrayResId = getResourceId(R.styleable.CustomListPreference_android_entries, 0)

            if (valuesArrayResId != 0 && entriesArrayResId != 0) {
                entryValues = context.resources.getStringArray(valuesArrayResId)
                entries = context.resources.getStringArray(entriesArrayResId)
            } else {
                recycle()
                return@apply
            }

            val savedLanguage = sharedPreferences.getString(key, LANGUAGE_EN)
            val defaultIndex = entryValues.indexOf(savedLanguage)
            summary = entries.getOrNull(defaultIndex) ?: getString(R.styleable.CustomListPreference_android_defaultValue)

            recycle()
        }

        setOnPreferenceClickListener {
            showCustomDialog(summary.toString())
            true
        }
    }

    private fun showCustomDialog(selectedValue: String) {
        val selectedIndex = entries.indexOf(selectedValue)

        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setSingleChoiceItems(entries, selectedIndex) { dialog, which ->
                val newValue = entries[which]
                if (callChangeListener(newValue)) {
                    setValue(newValue)
                }
                dialog.dismiss()
            }.setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun setValue(value: String?) {
        val index = entries.indexOf(value)
        val newValue = if (index != -1) entryValues[index] else null

        sharedPreferences.edit().putString(key, newValue).apply()
        summary = value
    }
}
