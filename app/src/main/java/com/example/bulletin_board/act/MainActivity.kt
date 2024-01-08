package com.example.bulletin_board.act

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels

import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bulletin_board.R
import com.example.bulletin_board.accounthelper.AccountHelper.Companion.RESULT_CODE_SUCCESS
import com.example.bulletin_board.adapters.AdsRcAdapter
import com.example.bulletin_board.databinding.ActivityMainBinding
import com.example.bulletin_board.dialoghelper.DialogConst
import com.example.bulletin_board.dialoghelper.DialogHelper
import com.example.bulletin_board.model.Announcement
import com.example.bulletin_board.viewmodel.FirebaseViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity(), OnNavigationItemSelectedListener, AdsRcAdapter.Listener{

    private lateinit var textViewAccount: TextView
    private lateinit var binding: ActivityMainBinding
    private val dialogHelper = DialogHelper(this)
    val mAuth = Firebase.auth
    val adapter = AdsRcAdapter(this)
    private val firebaseViewModel: FirebaseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        val toolbar: Toolbar = findViewById(R.id.toolbar)
//        setSupportActionBar(toolbar)

        init()
        initRecyclerView()
        initViewModel()
        firebaseViewModel.loadAllAnnouncement()
        bottomMenuOnClick()
    }

    override fun onResume() {
        super.onResume()
        binding.mainContent.bNavView.selectedItemId = R.id.id_home
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RESULT_CODE_SUCCESS) {
            // Обработка успешного результата
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            //Log.d("MyLog", "Sign in result ")

            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    dialogHelper.accHelper.signInFirebaseWithGoogle(account.idToken!!)
                }
            } catch (e: ApiException) {
                Log.d("MyLog", "Api exception: ${e.message} ")
            }

            super.onActivityResult(requestCode, resultCode, data)
        }
    }

//     val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//        if (result.resultCode == RESULT_CODE_SUCCESS) {
//            // Обработка успешного результата
//            val data: Intent? = result.data
//            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
//            //Log.d("MyLog", "Sign in result ")
//
//            try {
//                val account = task.getResult(ApiException::class.java)
//                if (account != null){
//                    dialogHelper.accHelper.signInFirebaseWithGoogle(account.idToken!!)
//                }
//            } catch (e: ApiException){
//                Log.d("MyLog", "Api exception: ${e.message} ")
//            }
//        } else {
//            Log.d("MyLog", "Sign in resultttt ")
//        }
//    }

    override fun onStart() {
        super.onStart()
        uiUpdate(mAuth.currentUser)
    }

    private fun initViewModel(){
        firebaseViewModel.liveAdsData.observe(this) {
            it?.let { it1 -> adapter.updateAdapter(it1) }
        }
    }

    private fun init() {
        setSupportActionBar(binding.mainContent.toolbar)
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.mainContent.toolbar,
            R.string.open,
            R.string.close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navigationView.setNavigationItemSelectedListener(this)
        textViewAccount =
            binding.navigationView.getHeaderView(0).findViewById(R.id.text_view_account_email)
    }

    private fun bottomMenuOnClick() = with(binding){
        mainContent.bNavView.setOnNavigationItemSelectedListener { item ->
        when(item.itemId) {
        R.id.id_new_ad -> {
            val i = Intent(this@MainActivity, EditAdsActivity::class.java)
            startActivity(i)
        }
        R.id.id_my_ads -> {
            firebaseViewModel.loadMyAnnouncement()
            mainContent.toolbar.title = getString(R.string.ad_my_ads)
        }
        R.id.id_favs -> {}
        R.id.id_home -> {
            firebaseViewModel.loadAllAnnouncement()
            mainContent.toolbar.title = getString(R.string.def)
        }
        }
            true

        }
    }

    private fun initRecyclerView(){
        binding.apply {
            mainContent.recyclerViewMainContent.layoutManager = LinearLayoutManager(this@MainActivity)
            mainContent.recyclerViewMainContent.adapter = adapter
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.id_my_ads -> {
                Toast.makeText(this, "pressed my ads", Toast.LENGTH_SHORT).show()
            }

            R.id.id_car -> {

            }

            R.id.id_pc -> {

            }

            R.id.id_smartphone -> {

            }

            R.id.id_dm -> {

            }

            R.id.id_sign_up -> {
                dialogHelper.createSignDialog(DialogConst.SIGN_UP_STATE)
            }

            R.id.id_sign_in -> {
                dialogHelper.createSignDialog(DialogConst.SIGN_IN_STATE)
            }

            R.id.id_sign_out -> {
                uiUpdate(null)
                mAuth.signOut()
                dialogHelper.accHelper.signOutGoogle()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun uiUpdate(user: FirebaseUser?) {
        textViewAccount.text = if (user == null) {
            resources.getString(R.string.not_reg)
        } else {
            user.email
        }
    }

    companion object{
        const val EDIT_STATE = "edit_state"
        const val ADS_DATA = "ads_data"
    }

    override fun onDeleteItem(ad: Announcement) {
        firebaseViewModel.deleteItem(ad)
    }

    override fun onAdViewed(ad: Announcement) {
        firebaseViewModel.adViewed(ad)
    }
}