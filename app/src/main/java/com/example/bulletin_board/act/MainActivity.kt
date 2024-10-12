package com.example.bulletin_board.act

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.bulletin_board.R
import com.example.bulletin_board.adapterFirestore.AdsAdapter
import com.example.bulletin_board.adapterFirestore.AppStateListener
import com.example.bulletin_board.adapterFirestore.FavoriteAdsAdapter
import com.example.bulletin_board.adapterFirestore.MyAdsAdapter
import com.example.bulletin_board.databinding.ActivityMainBinding
import com.example.bulletin_board.dialoghelper.DialogConst
import com.example.bulletin_board.dialoghelper.SignInDialogFragment
import com.example.bulletin_board.dialogs.DialogSpinnerHelper
import com.example.bulletin_board.dialogs.RcViewDialogSpinnerAdapter
import com.example.bulletin_board.dialogs.RcViewSearchSpinnerAdapter
import com.example.bulletin_board.domain.AccountManager
import com.example.bulletin_board.domain.AccountUiViewsProvider
import com.example.bulletin_board.domain.AdapterManager
import com.example.bulletin_board.domain.AdapterView
import com.example.bulletin_board.domain.AuthCallback
import com.example.bulletin_board.domain.DataAdapterManager
import com.example.bulletin_board.domain.FilterReader
import com.example.bulletin_board.domain.FilterUpdater
import com.example.bulletin_board.domain.NavigationViewHelper
import com.example.bulletin_board.domain.OrderByFilterDialog
import com.example.bulletin_board.domain.OrderByFilterDialogManager
import com.example.bulletin_board.domain.PermissionManager
import com.example.bulletin_board.domain.ResourceStringProvider
import com.example.bulletin_board.domain.SearchManager
import com.example.bulletin_board.domain.SearchQueryHandler
import com.example.bulletin_board.domain.SearchQueryHandlerCallback
import com.example.bulletin_board.domain.SearchUi
import com.example.bulletin_board.domain.SearchUiInitializer
import com.example.bulletin_board.domain.ThemeManager
import com.example.bulletin_board.domain.ToastHelper
import com.example.bulletin_board.domain.TokenSaveHandler
import com.example.bulletin_board.domain.VoiceRecognitionHandler
import com.example.bulletin_board.domain.VoiceRecognitionListener
import com.example.bulletin_board.model.Ad
import com.example.bulletin_board.model.AdItemClickListener
import com.example.bulletin_board.model.AdUpdateEvent
import com.example.bulletin_board.model.FavData
import com.example.bulletin_board.model.ViewData
import com.example.bulletin_board.packroom.RemoteAdDataSource.Companion.CATEGORY_FIELD
import com.example.bulletin_board.packroom.RemoteAdDataSource.Companion.KEYWORDS_FIELD
import com.example.bulletin_board.packroom.RemoteAdDataSource.Companion.ORDER_BY_FIELD
import com.example.bulletin_board.packroom.SortOption
import com.example.bulletin_board.settings.SettingsActivity
import com.example.bulletin_board.utils.SortUtils
import com.example.bulletin_board.viewmodel.FirebaseViewModel
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
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
    SearchUiInitializer,
    SearchUi,
    VoiceRecognitionListener,
    FilterUpdater,
    FilterReader,
    SearchQueryHandler,
    AccountUiViewsProvider,
    AdapterView,
    OrderByFilterDialog,
    AppStateListener {
    private val viewModel: FirebaseViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    @Inject lateinit var orderByFilterDialogManager: OrderByFilterDialogManager

    @Inject lateinit var sortUtils: SortUtils

    @Inject lateinit var voiceRecognitionHandler: VoiceRecognitionHandler

    @Inject lateinit var searchManager: SearchManager

    @Inject lateinit var accountManager: AccountManager

    private var lastClickTime: Long = 0
    private val doubleClickThreshold = 300
    private val scrollStateMap = mutableMapOf<Int, Parcelable?>()
    private var currentTabPosition: Int = 0

    private val favAdsAdapter by lazy { FavoriteAdsAdapter(this) }
    private val adsAdapter by lazy { AdsAdapter(this) }
    private val myAdsAdapter by lazy { MyAdsAdapter(this) }

    private val filterFragment by lazy { FilterFragment() }

    private val voiceRecognitionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            voiceRecognitionHandler.handleRecognitionResult(result)
        }

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            handleGoogleSignInResult(result)
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(ThemeManager.getSelectedTheme(PreferenceManager.getDefaultSharedPreferences(this)))
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initFilters()
        initAdapters()
        requestNotificationPermission()
        accountManager.init()
        initViewModel()
        initUiComponents()
        setupSearchFunctionality()
        setupBottomMenu()
    }

    private fun initFilters() {
        val newFilters = mapOf(CATEGORY_FIELD to "", ORDER_BY_FIELD to SortOption.BY_NEWEST.id)
        viewModel.updateFilters(newFilters)
    }

    private fun initAdapters() {
        AdapterManager.registerAdapters(
            ADS_ADAPTER to adsAdapter,
            FAV_ADAPTER to favAdsAdapter,
            MY_ADAPTER to myAdsAdapter,
        )
        AdapterManager.initRecyclerView(
            binding.mainContent.recyclerViewMainContent,
            adsAdapter,
            this,
        )
    }

    @SuppressLint("NewApi")
    private fun requestNotificationPermission() {
        PermissionManager.checkAndRequestNotificationPermission(this)
    }

    private fun initViewModel() {
        lifecycleScope.launch {
            viewModel.appState.collectLatest { event ->
                updateSortOption(event.filter)
            }
        }
        initAdapterData()
    }

    private fun updateSortOption(filter: Map<String, String>) {
        if (filter.isNotEmpty()) {
            val sortOptionId = filter[ORDER_BY_FIELD] ?: return
            val sortOptionText = sortUtils.getSortOptionText(sortOptionId)
            binding.mainContent.autoComplete.setText(sortOptionText)
        }
    }

    private fun initAdapterData() {
        lifecycleScope.launch {
            launch { DataAdapterManager.handleAdapterData(viewModel.favoriteAds, favAdsAdapter, this@MainActivity) }
            launch { DataAdapterManager.handleAdapterData(viewModel.homeAdsData, adsAdapter, this@MainActivity) }
            launch { DataAdapterManager.handleAdapterData(viewModel.myAds, myAdsAdapter, this@MainActivity) }
        }
    }

    private fun initUiComponents() {
        setSupportActionBar(binding.mainContent.searchBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.navigationView.setNavigationItemSelectedListener(this)
        binding.mainContent.filterButtonMain.setOnClickListener { showFilterFragment() }
        binding.mainContent.swipeRefreshLayout.setOnRefreshListener { refreshCurrentAdapter() }

        binding.mainContent.floatingActButton.setOnClickListener {
            val intent = Intent(this, EditAdsActivity::class.java)
            startActivity(intent)
        }

        navViewSetting()
    }

    private fun showFilterFragment() {
        if (!filterFragment.isAdded) {
            filterFragment.show(supportFragmentManager, FilterFragment.TAG)
        }
    }

    private fun refreshCurrentAdapter() {
        AdapterManager.refreshAdapter(binding.mainContent.bottomNavView.selectedItemId)
        binding.mainContent.swipeRefreshLayout.isRefreshing = false
    }

    private fun setupSearchFunctionality() {
        orderByFilterDialogManager.setupOrderByFilter(binding.mainContent.autoComplete)
        searchManager.initializeSearchFunctionality()
    }

    private fun handleGoogleSignInResult(result: ActivityResult) {
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

    private fun setupBottomMenu() {
        with(binding) {
            mainContent.bottomNavView.setOnItemSelectedListener { item ->
                handleBottomNavigation(item)
                true
            }
        }
    }

    private fun handleBottomNavigation(item: MenuItem) {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastClickTime <= doubleClickThreshold) {
            AdapterManager.refreshAdapter(item.itemId)
        }
        lastClickTime = currentTime

        when (item.itemId) {
            R.id.id_settings -> startActivity(Intent(this, SettingsActivity::class.java))
            R.id.id_my_ads -> switchAdapter(MY_ADAPTER)
            R.id.id_favs -> switchAdapter(FAV_ADAPTER)
            R.id.id_home -> switchAdapter(ADS_ADAPTER)
        }
    }

    private fun switchAdapter(tabPosition: Int) {
        AdapterManager.switchAdapter(this, tabPosition, scrollStateMap, currentTabPosition)
        currentTabPosition = tabPosition
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        handleDrawerNavigation(item)
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun handleDrawerNavigation(item: MenuItem) {
        when (item.itemId) {
            R.id.id_my_ads -> Toast.makeText(this, "pressed my ads", Toast.LENGTH_SHORT).show()
            R.id.id_car, R.id.id_pc, R.id.id_smartphone, R.id.id_dm -> {
                getAdsFromCat(sortUtils.getCategoryFromItem(item.itemId))
            }
            R.id.id_sign_up -> showSignInDialog(DialogConst.SIGN_UP_STATE)
            R.id.id_sign_in -> showSignInDialog(DialogConst.SIGN_IN_STATE)
            R.id.id_sign_out -> handleSignOut()
        }
    }

    private fun showSignInDialog(state: Int) {
        SignInDialogFragment(
            googleSignInLauncher,
            state,
            accountManager,
            accountManager,
            this,
        ).show(supportFragmentManager, "SignInDialog")
    }

    private fun handleSignOut() {
        if (accountManager.isAnonymous()) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            return
        }
        accountManager.updateUi(null)
        accountManager.signOut()
        accountManager.signOutGoogle()
    }

    private fun getAdsFromCat(category: String) {
        val newFilters =
            mapOf(
                CATEGORY_FIELD to category,
                ORDER_BY_FIELD to viewModel.getFilterValue(ORDER_BY_FIELD)!!,
            )
        Timber.d("New filters applied in getAdsFromCat")
        viewModel.updateFilters(newFilters)
    }

    private fun navViewSetting() {
        val menu = binding.navigationView.menu
        val colorPrimary = R.color.md_theme_light_primary

        NavigationViewHelper.setMenuItemStyle(menu, R.id.adsCat, colorPrimary, this)
        NavigationViewHelper.setMenuItemStyle(menu, R.id.accCat, colorPrimary, this)
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
            R.id.id_search -> handleSearchIconClick(item)
            R.id.id_voice -> {
                voiceRecognitionHandler.startVoiceRecognition(voiceRecognitionLauncher)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun handleSearchIconClick(item: MenuItem) {
        if (!viewModel.getFilterValue(KEYWORDS_FIELD).isNullOrEmpty()) {
            binding.mainContent.searchBar.clearText()
            viewModel.addToFilter(KEYWORDS_FIELD, "")
            item.setIcon(R.drawable.ic_search)
        } else {
            binding.mainContent.searchBar.performClick()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionManager.handleRequestPermissionsResult(requestCode, grantResults, this)
    }

    override fun onResume() {
        super.onResume()
        binding.mainContent.bottomNavView.selectedItemId = R.id.id_home
    }

    override fun onStart() {
        super.onStart()
        accountManager.updateUi(accountManager.auth.currentUser)
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

    override fun handleSearchQuery(
        inputSearchQuery: String,
        callback: SearchQueryHandlerCallback,
    ) {
        lifecycleScope.launch {
            viewModel.fetchSearchResults(inputSearchQuery)
            viewModel.appState.collectLatest { appState ->
                if (appState.searchResults.isNotEmpty()) {
                    val formattedResults =
                        viewModel.formatSearchResults(appState.searchResults, inputSearchQuery)
                    callback.onSearchResultsUpdated(formattedResults)
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

    override fun getTextViewAccount(): TextView = binding.navigationView.getHeaderView(0).findViewById(R.id.text_view_account_email)

    override fun getImageViewAccount(): ImageView = binding.navigationView.getHeaderView(0).findViewById(R.id.image_view_account_image)

    override val nothinkWhiteAnim: LottieAnimationView
        get() = binding.mainContent.nothinkWhiteAnim

    override val recyclerViewMainContent: RecyclerView
        get() = binding.mainContent.recyclerViewMainContent

    override fun showSpinnerPopup(
        anchorView: View,
        list: ArrayList<Pair<String, String>>,
        tvSelection: TextView,
        onItemSelectedListener: RcViewDialogSpinnerAdapter.OnItemSelectedListener?,
        isSearchable: Boolean,
    ) {
        DialogSpinnerHelper.showSpinnerPopup(
            this,
            anchorView,
            list,
            tvSelection,
            onItemSelectedListener,
            isSearchable,
        )
    }

    override fun onAppStateEvent(adEvent: (AdUpdateEvent) -> Unit) {
        viewModel.viewModelScope.launch {
            viewModel.appState.drop(1).collectLatest { event ->
                event.adEvent?.let { adEvent ->
                    adEvent(adEvent)
                }
            }
        }
    }
}
