package com.example.bulletin_board.settings

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import com.example.bulletin_board.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CustomListPreference(
    context: Context?,
    attrs: AttributeSet?,
) : Preference(context!!, attrs) {
    private val sharedPreferences = context?.let { PreferenceManager.getDefaultSharedPreferences(it) }
    private var entryValues: Array<String>? = null
    private var entries: Array<String>? = null

    init {
        val typedArray = context?.obtainStyledAttributes(attrs, R.styleable.CustomListPreference)

        // Установим начальное значение в summary
        val defaultValueFromXml = typedArray?.getString(R.styleable.CustomListPreference_android_defaultValue)
        Log.d("LOOOOG1", "defaultValueFromXml = $defaultValueFromXml")

        val selectedValue = sharedPreferences?.getString(key, defaultValueFromXml)
        Log.d("LOOOOG2", "selectedValue = $selectedValue")

        summary = selectedValue

        // Получаем ресурсы для массивов
        val valuesArrayResId = typedArray?.getResourceId(R.styleable.CustomListPreference_android_entryValues, 0)
        val entriesArrayResId = typedArray?.getResourceId(R.styleable.CustomListPreference_android_entries, 0)

// Устанавливаем значения массивов
        if (context != null) {
            valuesArrayResId?.let { context.resources.getStringArray(it) }?.let {
                entriesArrayResId?.let { context.resources.getStringArray(it) }?.let { it1 ->
                    setEntryValuesAndEntries(
                        it,
                        it1,
                    )
                }
            }
        }

        typedArray?.recycle()

        setOnPreferenceClickListener {
            showCustomDialog(selectedValue)
            true
        }
    }

    fun setEntryValuesAndEntries(
        entryValues: Array<String>,
        entries: Array<String>,
    ) {
        this.entryValues = entryValues
        this.entries = entries
    }

    private fun showCustomDialog(selectedValue: String?) {
        Log.d("LOOOOG", "entries = ${entries?.contentToString()}")
        Log.d("LOOOOG", "entryValues = ${entryValues?.contentToString()}")

        // val selectedValue = sharedPreferences?.getString(key, "defaultValue") ?: "defaultValue"

        val selectedIndex = entryValues?.indexOf(selectedValue) ?: -1

        Log.d("LOOOOG", "selectedIndex = $selectedIndex")

        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setSingleChoiceItems(entries, selectedIndex) { dialog, which ->
                val newValue = entryValues?.get(which)
                if (callChangeListener(newValue)) {
                    setValue(newValue)
                }
                dialog.dismiss()
            }.setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun setValue(value: String?) {
        Log.d("LOOOOG5", "value = $value")
        sharedPreferences?.edit()?.putString(key, value)?.apply()

        // Обновляем summary при изменении значения
        summary = value
    }
}
