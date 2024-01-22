package com.example.bulletin_board.act

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels

import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.bulletin_board.R
import com.example.bulletin_board.accounthelper.AccountHelper
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

class MainActivity : AppCompatActivity(), OnNavigationItemSelectedListener, AdsRcAdapter.Listener {

    private lateinit var textViewAccount: TextView
    private lateinit var imageViewAccount: ImageView
    private lateinit var binding: ActivityMainBinding
    private val dialogHelper = DialogHelper(this)
    val mAuth = Firebase.auth
    val adapter = AdsRcAdapter(this)
    lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private val firebaseViewModel: FirebaseViewModel by viewModels()
    private var clearUpdate: Boolean = true
    private var currentCategory: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        val toolbar: Toolbar = findViewById(R.id.toolbar)
//        setSupportActionBar(toolbar)

        init()
        initRecyclerView()
        initViewModel()
        bottomMenuOnClick()
        scrollListener()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.id_filter) startActivity(Intent(this@MainActivity, FilterActivity::class.java))
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        binding.mainContent.bNavView.selectedItemId = R.id.id_home
    }

    private fun onActivityResult() {
        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)

            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    dialogHelper.accHelper.signInFirebaseWithGoogle(account.idToken!!)
                }
            } catch (e: ApiException) {
                Log.d("MyLog", "Api exception: ${e.message} ")
            }
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

    private fun initViewModel() {
        firebaseViewModel.liveAdsData.observe(this) {
            it?.let { content ->
                val list = getAdsByCategory(content)
                if (!clearUpdate) adapter.updateAdapter(list) else adapter.updateAdapterWithClear(list)
                if (adapter.itemCount == 0) {
                    binding.mainContent.recyclerViewMainContent.visibility = View.INVISIBLE
                    binding.mainContent.nothinkWhiteAnim.visibility = View.VISIBLE
                    binding.mainContent.nothinkWhiteAnim.repeatCount = LottieDrawable.INFINITE
                    binding.mainContent.nothinkWhiteAnim.playAnimation()
                } else {
                    binding.mainContent.recyclerViewMainContent.visibility = View.VISIBLE
                    binding.mainContent.nothinkWhiteAnim.cancelAnimation()
                    binding.mainContent.nothinkWhiteAnim.visibility = View.GONE
                }
            }
        }
    }

    private fun getAdsByCategory(list: ArrayList<Announcement>): ArrayList<Announcement>{
        val tempList = ArrayList<Announcement>()
        tempList.addAll(list)
        if (currentCategory != getString(R.string.def)){
            tempList.clear()
            list.forEach{
                if (currentCategory == it.category) tempList.add(it)
            }
        }
        tempList.reverse()
        return tempList
    }

    private fun init() {
        currentCategory = getString(R.string.def)
        setSupportActionBar(binding.mainContent.toolbar)
        onActivityResult()
        navViewSetting()
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
        imageViewAccount =
            binding.navigationView.getHeaderView(0).findViewById(R.id.image_view_account_image)
    }

    private fun bottomMenuOnClick() = with(binding) {
        mainContent.bNavView.setOnNavigationItemSelectedListener { item ->
            clearUpdate = true
            when (item.itemId) {
                R.id.id_new_ad -> {
                    val i = Intent(this@MainActivity, EditAdsActivity::class.java)
                    startActivity(i)
                }

                R.id.id_my_ads -> {
                    firebaseViewModel.loadMyAnnouncement()
                    mainContent.toolbar.title = getString(R.string.ad_my_ads)
                }

                R.id.id_favs -> {
                    firebaseViewModel.loadMyFavs()
                    mainContent.toolbar.title = getString(R.string.favs)
                }

                R.id.id_home -> {
                    currentCategory = getString(R.string.def)
                    firebaseViewModel.loadAllAnnouncementFirstPage()
                    mainContent.toolbar.title = getString(R.string.def)
                }
            }
            true

        }
    }

    private fun initRecyclerView() {
        binding.apply {
            mainContent.recyclerViewMainContent.layoutManager =
                LinearLayoutManager(this@MainActivity)
            mainContent.recyclerViewMainContent.adapter = adapter
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        clearUpdate = true
        when (item.itemId) {

            R.id.id_my_ads -> {
                Toast.makeText(this, "pressed my ads", Toast.LENGTH_SHORT).show()
            }

            R.id.id_car -> {
                getAdsFromCat(getString(R.string.ad_car))
            }

            R.id.id_pc -> {
                getAdsFromCat(getString(R.string.ad_pc))
            }

            R.id.id_smartphone -> {
                getAdsFromCat(getString(R.string.ad_smartphone))
            }

            R.id.id_dm -> {
                getAdsFromCat(getString(R.string.ad_dm))
            }

            R.id.id_sign_up -> {
                dialogHelper.createSignDialog(DialogConst.SIGN_UP_STATE)
            }

            R.id.id_sign_in -> {
                dialogHelper.createSignDialog(DialogConst.SIGN_IN_STATE)
            }

            R.id.id_sign_out -> {
                if (mAuth.currentUser?.isAnonymous == true) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    return true
                }
                uiUpdate(null)
                mAuth.signOut()
                dialogHelper.accHelper.signOutGoogle()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun getAdsFromCat(cat: String){
        currentCategory = cat
        firebaseViewModel.loadAllAnnouncementFromCatFirstPage(cat)
    }

    fun uiUpdate(user: FirebaseUser?) {
        if (user == null) {
            dialogHelper.accHelper.signInAnonymously(object : AccountHelper.Listener {
                override fun onComplete() {
                    textViewAccount.text = "Гость"
                    imageViewAccount.setImageResource(R.drawable.ic_my_ads)
                }

            })
        } else if (user.isAnonymous) {
            textViewAccount.text = "Гость"
            imageViewAccount.setImageResource(R.drawable.ic_my_ads)
        } else if (!user.isAnonymous) {
            textViewAccount.text = user.email
            Glide.with(this)
                .load(user.photoUrl)
                .apply(RequestOptions().transform(RoundedCorners(20)))
                .into(imageViewAccount)
        }
    }

    override fun onDeleteItem(ad: Announcement) {
        firebaseViewModel.deleteItem(ad)
    }

    override fun onAdViewed(ad: Announcement) {
        firebaseViewModel.adViewed(ad)
        val i = Intent(this, DescriptionActivity::class.java)
        i.putExtra("AD", ad)
        startActivity(i)
    }

    override fun onFavClicked(ad: Announcement) {
        firebaseViewModel.onFavClick(ad)
    }

    private fun navViewSetting() = with(binding) {
        val menu = navigationView.menu
        val adsCat = menu.findItem(R.id.adsCat)
        val spanAdsCat = SpannableString(adsCat.title)

        val colorPrimary = R.color.md_theme_light_primary

        adsCat.title?.let {
            spanAdsCat.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        this@MainActivity,
                        colorPrimary
                    )
                ), 0, it.length, 0
            )
        }
        adsCat.title = spanAdsCat

        val accCat = menu.findItem(R.id.accCat)
        val spanAccCat = SpannableString(accCat.title)
        accCat.title?.let {
            spanAccCat.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        this@MainActivity,
                        colorPrimary
                    )
                ), 0, it.length, 0
            )
        }
        accCat.title = spanAccCat
    }

    private fun scrollListener() = with(binding.mainContent) {
        recyclerViewMainContent.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(SCROLL_DOWN) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    clearUpdate = false
                    val adsList = firebaseViewModel.liveAdsData.value!!
                    if (adsList.isNotEmpty()) {
                        getAdsFromCat(adsList)
                    }
                }
            }
        })
    }

    private fun getAdsFromCat(adsList: ArrayList<Announcement>){
        adsList[0].let{
            if (currentCategory == getString(R.string.def)){
                firebaseViewModel.loadAllAnnouncementNextPage(it.time)
            }else{
                val catTime = "${it.category}_${it.time}"
                firebaseViewModel.loadAllAnnouncementFromCatNextPage(catTime)
            }
        }
    }

    companion object {
        const val EDIT_STATE = "edit_state"
        const val ADS_DATA = "ads_data"
        const val SCROLL_DOWN = 1
    }
}