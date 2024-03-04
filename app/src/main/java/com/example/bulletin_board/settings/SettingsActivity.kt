package com.example.bulletin_board.settings

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.bulletin_board.R
import com.example.bulletin_board.act.MainActivity
import com.example.bulletin_board.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var defPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        defPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        setTheme(getSelectedTheme())

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isEnabled = true
                startMainActivity()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        if (savedInstanceState == null){
            supportFragmentManager.beginTransaction().replace(R.id.placeHolder, SettingsFragment()).commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home){
            startMainActivity()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("fragment_to_display", "fragment1")
        startActivity(intent)
    }

    private fun getSelectedTheme(): Int{
        return if (defPreferences.getString("theme_key", "light") == "light"){
            R.style.Base_Theme_Bulletin_board_light
        }else{
            R.style.Base_Theme_Bulletin_board_dark
        }
    }

}