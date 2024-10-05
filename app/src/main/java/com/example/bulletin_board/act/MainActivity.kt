package com.example.bulletin_board.act

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.bulletin_board.R
import com.example.bulletin_board.adapterFirestore.AdsAdapter
import com.example.bulletin_board.adapterFirestore.FavoriteAdsAdapter
import com.example.bulletin_board.adapterFirestore.MyAdsAdapter
import com.example.bulletin_board.databinding.ActivityMainBinding
import com.example.bulletin_board.dialoghelper.DialogConst
import com.example.bulletin_board.dialoghelper.SignInDialogFragment
import com.example.bulletin_board.dialogs.RcViewSearchSpinnerAdapter
import com.example.bulletin_board.domain.AccountManager
import com.example.bulletin_board.domain.AdapterManager
import com.example.bulletin_board.domain.AdapterViewManager
import com.example.bulletin_board.domain.AuthCallback
import com.example.bulletin_board.domain.DataAdapterManager
import com.example.bulletin_board.domain.FilterReader
import com.example.bulletin_board.domain.ImageLoader
import com.example.bulletin_board.domain.NavigationViewHelper
import com.example.bulletin_board.domain.OrderByFilterDialogManager
import com.example.bulletin_board.domain.PermissionManager
import com.example.bulletin_board.domain.ResourceStringProvider
import com.example.bulletin_board.domain.SearchAdapterUpdateCallback
import com.example.bulletin_board.domain.SearchAdapterUpdater
import com.example.bulletin_board.domain.SearchManager
import com.example.bulletin_board.domain.SearchUiInitializer
import com.example.bulletin_board.domain.ThemeManager
import com.example.bulletin_board.domain.ToastHelper
import com.example.bulletin_board.domain.TokenSaveHandler
import com.example.bulletin_board.domain.VoiceRecognitionHandler
import com.example.bulletin_board.domain.VoiceRecognitionListener
import com.example.bulletin_board.model.Ad
import com.example.bulletin_board.model.AdItemClickListener
import com.example.bulletin_board.model.FavData
import com.example.bulletin_board.model.ViewData
import com.example.bulletin_board.packroom.RemoteAdDataSource.Companion.CATEGORY_FIELD
import com.example.bulletin_board.packroom.RemoteAdDataSource.Companion.KEYWORDS_FIELD
import com.example.bulletin_board.packroom.RemoteAdDataSource.Companion.ORDER_BY_FIELD
import com.example.bulletin_board.packroom.SortOption
import com.example.bulletin_board.settings.SettingsActivity
import com.example.bulletin_board.utils.FilterUpdater
import com.example.bulletin_board.utils.SearchUi
import com.example.bulletin_board.utils.SortUtils.getSortOptionText
import com.example.bulletin_board.viewmodel.FirebaseViewModel
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class MainActivity :
    AppCompatActivity(),
    OnNavigationItemSelectedListener,
    AdItemClickListener,
    TokenSaveHandler,
    ToastHelper,
    ResourceStringProvider,
    ImageLoader,
    SearchUiInitializer,
    SearchUi,
    SearchAdapterUpdater,
    VoiceRecognitionListener,
    FilterUpdater,
    FilterReader {
    private val viewModel: FirebaseViewModel by viewModels()

    private lateinit var binding: ActivityMainBinding

    private lateinit var orderByFilterDialogManager: OrderByFilterDialogManager // Inject

    private var lastClickTime: Long = 0
    private val doubleClickThreshold = 300

    @Inject
    lateinit var voiceRecognitionHandler: VoiceRecognitionHandler

    private val voiceRecognitionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            voiceRecognitionHandler.handleRecognitionResult(result)
        }

    private val favAdsAdapter by lazy {
        FavoriteAdsAdapter(viewModel)
    }

    private val adsAdapter by lazy {
        AdsAdapter(viewModel)
    }

    private val myAdsAdapter by lazy {
        MyAdsAdapter(viewModel)
    }

    private val scrollStateMap = mutableMapOf<Int, Parcelable?>()
    private var currentTabPosition: Int = 0

    private val filterFragment by lazy { FilterFragment() }

    @Inject
    private lateinit var searchManager: SearchManager

    @Inject
    lateinit var accountManager: AccountManager

    private val googleSignInLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            accountManager.handleGoogleSignInResult(
                result,
                object : AuthCallback {
                    override fun onAuthComplete(user: FirebaseUser?) {
                        accountManager.updateUi(user)
                    }

                    override fun onSaveToken(token: String) {
                        accountManager.saveToken(token)
                    }
                },
            )
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(ThemeManager.getSelectedTheme(PreferenceManager.getDefaultSharedPreferences(this)))
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AdapterManager.registerAdapter(0, adsAdapter)
        AdapterManager.registerAdapter(1, favAdsAdapter)
        AdapterManager.registerAdapter(2, myAdsAdapter)

        AdapterViewManager.initViews(binding)

        accountManager.initUi(binding)

        AdapterManager.initRecyclerView(binding.mainContent.recyclerViewMainContent, adsAdapter, this)

        PermissionManager.checkAndRequestNotificationPermission(this)

        initViewModel()
        init()

        orderByFilterDialogManager = OrderByFilterDialogManager(this, searchManager, searchManager)
        orderByFilterDialogManager.setupOrderByFilter(this, binding.mainContent.autoComplete)
        searchManager.initSearchAdd()
        setupBottomMenu()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionManager.handleRequestPermissionsResult(requestCode, grantResults, this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }

            R.id.id_search -> {
                if (!viewModel.getFilterValue(KEYWORDS_FIELD).isNullOrEmpty()) {
                    binding.mainContent.searchBar.clearText()
                    viewModel.addToFilter(KEYWORDS_FIELD, "")
                    item.setIcon(R.drawable.ic_search)
                } else {
                    binding.mainContent.searchBar.performClick()
                }
                return true
            }

            R.id.id_voice -> {
                voiceRecognitionHandler.startVoiceRecognition(voiceRecognitionLauncher)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        binding.mainContent.bottomNavView.selectedItemId = R.id.id_home
    }

    override fun onStart() {
        super.onStart()
        accountManager.updateUi(accountManager.auth.currentUser)
    }

    private fun initViewModel() {
        lifecycleScope.launch {
            viewModel.appState.collectLatest { event ->
                if (event.filter.isNotEmpty()) {
                    val sortOptionId = event.filter[ORDER_BY_FIELD] ?: return@collectLatest
                    val sortOptionText = getSortOptionText(this@MainActivity, sortOptionId)
                    binding.mainContent.autoComplete.setText(sortOptionText)
                }
            }
        }

        lifecycleScope.launch {
            launch { DataAdapterManager.handleAdapterData(viewModel.favoriteAds, favAdsAdapter, AdapterViewManager) }
            launch { DataAdapterManager.handleAdapterData(viewModel.homeAdsData, adsAdapter, AdapterViewManager) }
            launch { DataAdapterManager.handleAdapterData(viewModel.myAds, myAdsAdapter, AdapterViewManager) }
        }
    }

    private fun init() {
        Timber.d("init() called")
        val newFilters =
            mapOf(
                CATEGORY_FIELD to "",
                ORDER_BY_FIELD to SortOption.BY_NEWEST.id,
            )
        viewModel.updateFilters(newFilters)
        setSupportActionBar(binding.mainContent.searchBar)

        navViewSetting()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.navigationView.setNavigationItemSelectedListener(this)

        binding.mainContent.filterButtonMain.setOnClickListener {
            if (!filterFragment.isAdded) {
                filterFragment.show(supportFragmentManager, FilterFragment.TAG)
            }
        }

        binding.mainContent.swipeRefreshLayout.setOnRefreshListener {
            AdapterManager.refreshAdapter(binding.mainContent.bottomNavView.selectedItemId)
            binding.mainContent.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun setupBottomMenu() {
        with(binding) {
            mainContent.bottomNavView.setOnItemSelectedListener { item ->
                val currentTime = System.currentTimeMillis()

                if (currentTime - lastClickTime <= doubleClickThreshold) {
                    AdapterManager.refreshAdapter(item.itemId)
                }
                lastClickTime = currentTime

                when (item.itemId) {
                    R.id.id_settings ->
                        startActivity(
                            Intent(
                                this@MainActivity,
                                SettingsActivity::class.java,
                            ),
                        )

                    R.id.id_my_ads -> switchAdapter(MY_ADAPTER)

                    R.id.id_favs -> switchAdapter(FAV_ADAPTER)

                    R.id.id_home -> switchAdapter(ADS_ADAPTER)
                }
                true
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.id_my_ads -> {
                Toast.makeText(this, "pressed my ads", Toast.LENGTH_SHORT).show()
            }

            R.id.id_car, R.id.id_pc, R.id.id_smartphone, R.id.id_dm -> {
                getAdsFromCat(
                    when (item.itemId) {
                        R.id.id_car -> SortOption.AD_CAR.id
                        R.id.id_pc -> SortOption.AD_PC.id
                        R.id.id_smartphone -> SortOption.AD_SMARTPHONE.id
                        R.id.id_dm -> SortOption.AD_DM.id
                        else -> ""
                    },
                )
            }

            R.id.id_sign_up -> {
                SignInDialogFragment(
                    googleSignInLauncher,
                    DialogConst.SIGN_UP_STATE,
                    accountManager,
                    accountManager,
                    this,
                ).show(supportFragmentManager, "SignUpDialog")
            }

            R.id.id_sign_in -> {
                SignInDialogFragment(
                    googleSignInLauncher,
                    DialogConst.SIGN_IN_STATE,
                    accountManager,
                    accountManager,
                    this,
                ).show(supportFragmentManager, "SignInDialog")
            }

            R.id.id_sign_out -> {
                if (accountManager.isAnonymous()) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    return true
                }
                accountManager.updateUi(null)
                accountManager.signOut()
                accountManager.signOutGoogle()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun getAdsFromCat(cat: String) {
        val newFilters =
            mapOf(
                CATEGORY_FIELD to cat,
                ORDER_BY_FIELD to (
                    viewModel.getFilterValue(ORDER_BY_FIELD)
                        ?: SortOption.BY_NEWEST.id
                ),
            )
        Timber.d("newFilters called getAdsFromCat")
        viewModel.updateFilters(newFilters)
    }

    private fun navViewSetting() {
        val menu = binding.navigationView.menu
        val colorPrimary = R.color.md_theme_light_primary

        NavigationViewHelper.setMenuItemStyle(menu, R.id.adsCat, colorPrimary, this)
        NavigationViewHelper.setMenuItemStyle(menu, R.id.accCat, colorPrimary, this)
    }

    private fun switchAdapter(tabPosition: Int) {
        AdapterManager.switchAdapter(AdapterViewManager, tabPosition, scrollStateMap, currentTabPosition)
        currentTabPosition = tabPosition
    }

    companion object {
        const val EDIT_STATE = "edit_state"
        const val ADS_DATA = "ads_data"
        const val ADS_ADAPTER = 0
        const val FAV_ADAPTER = 1
        const val MY_ADAPTER = 2
    }

    override fun onAdClick(ad: Ad) {
        Intent(this, DescriptionActivity::class.java).also {
            it.putExtra("AD", ad)
            startActivity(it)
        }
        viewModel.viewModelScope.launch {
            viewModel.adViewed(ViewData(ad.key, ad.viewsCounter))
        }
    }

    override fun onFavClick(favData: FavData) {
        viewModel.viewModelScope.launch {
            viewModel.onFavClick(favData)
        }
    }

    override fun onDeleteClick(adKey: String) {
        viewModel.viewModelScope.launch {
            viewModel.deleteAd(adKey)
        }
    }

    override fun onEditClick(ad: Ad) {
        Intent(this, EditAdsActivity::class.java).also {
            it.putExtra(EDIT_STATE, true)
            it.putExtra(ADS_DATA, ad)
            startActivity(it)
        }
    }

    override fun isOwner(adUid: String): Boolean = adUid == accountManager.auth.currentUser?.uid

    override fun saveToken(token: String) {
        viewModel.viewModelScope.launch {
            viewModel.saveTokenFCM(token)
        }
    }

    override fun showToast(
        message: String,
        duration: Int,
    ) {
        Toast.makeText(this, message, duration).show()
    }

    override fun getStringImpl(resId: Int): String = this.getString(resId)

    override fun loadImage(
        imageView: ImageView,
        imageUrl: Uri?,
        requestOptions: RequestOptions,
    ) {
        Glide
            .with(this)
            .load(imageUrl)
            .apply(requestOptions)
            .into(imageView)
    }

    override fun initSearchAdapter(item: String) {
        binding.mainContent.searchViewMainContent.editText
            .setText(item)
        binding.mainContent.searchViewMainContent.editText
            .setSelection(binding.mainContent.searchViewMainContent.editText.text.length)
    }

    override fun initRecyclerView(adapter: RcViewSearchSpinnerAdapter) {
        binding.mainContent.recyclerViewSearch.layoutManager = LinearLayoutManager(this)
        binding.mainContent.recyclerViewSearch.adapter = adapter
    }

    override fun updateSearchBar(query: String) {
        binding.mainContent.searchBar.setText(query)
        binding.mainContent.searchBar.menu
            .findItem(R.id.id_search)
            .setIcon(R.drawable.ic_cancel)
    }

    override fun hideSearchView() {
        binding.mainContent.searchViewMainContent.hide()
    }

    override fun addTextWatcher(textWatcher: TextWatcher) {
        binding.mainContent.searchViewMainContent.editText
            .addTextChangedListener(textWatcher)
    }

    override fun setSearchActionListener(listener: () -> Boolean) {
        binding.mainContent.searchViewMainContent.editText
            .setOnEditorActionListener { _, _, _ -> listener() }
    }

    override fun setSearchBarClickListener(listener: View.OnClickListener) {
        binding.mainContent.searchBar.setOnClickListener(listener)
    }

    override fun getQueryText(): String =
        binding.mainContent.searchViewMainContent.editText.text
            .toString()

    override fun setQueryText(text: String) {
        binding.mainContent.searchViewMainContent.editText
            .setText(text)
    }

    override fun getSearchBarText(): String =
        binding.mainContent.searchBar.text
            .toString()

    override fun updateAdapter(
        query: String,
        callback: SearchAdapterUpdateCallback,
    ) {
        lifecycleScope.launch {
            viewModel.fetchSearchResults(query)
            viewModel.appState.collectLatest { appState ->
                if (appState.searchResults.isNotEmpty()) {
                    val formattedResults = viewModel.formatSearchResults(appState.searchResults, query)
                    callback.onAdapterUpdated(formattedResults)
                }
            }
        }
    }

    override fun onVoiceRecognitionResult(spokenText: String) {
        binding.mainContent.searchBar.setText(spokenText)
        binding.mainContent.searchBar.menu
            .findItem(R.id.id_search)
            .setIcon(R.drawable.ic_cancel)

        val validateText = spokenText.split(" ").joinToString("-")
        viewModel.addToFilter(KEYWORDS_FIELD, validateText)
    }

    override fun addToFilter(
        key: String,
        value: String,
    ) {
        viewModel.addToFilter(key, value)
    }

    override fun getFilterValue(key: String): String? = viewModel.getFilterValue(key)
}
