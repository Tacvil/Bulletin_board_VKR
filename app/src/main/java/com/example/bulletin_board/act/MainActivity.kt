package com.example.bulletin_board.act

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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.PagingDataAdapter
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.bulletin_board.R
import com.example.bulletin_board.accounthelper.AccountHelper
import com.example.bulletin_board.accounthelper.AccountHelperProvider
import com.example.bulletin_board.accounthelper.UserUiUpdate
import com.example.bulletin_board.adapterFirestore.AdsAdapter
import com.example.bulletin_board.adapterFirestore.FavoriteAdsAdapter
import com.example.bulletin_board.adapterFirestore.MyAdsAdapter
import com.example.bulletin_board.databinding.ActivityMainBinding
import com.example.bulletin_board.dialoghelper.DialogConst
import com.example.bulletin_board.dialoghelper.DialogHelperProvider
import com.example.bulletin_board.dialoghelper.SignInDialogFragment
import com.example.bulletin_board.dialogs.RcViewSearchSpinnerAdapter
import com.example.bulletin_board.domain.NavigationViewHelper
import com.example.bulletin_board.domain.OrderByFilterDialogManager
import com.example.bulletin_board.domain.PermissionManager
import com.example.bulletin_board.domain.ThemeManager
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
import com.example.bulletin_board.utils.SearchActions
import com.example.bulletin_board.utils.SearchHelper
import com.example.bulletin_board.utils.SearchUi
import com.example.bulletin_board.utils.SortUtils.getSortOptionText
import com.example.bulletin_board.viewmodel.FirebaseViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class MainActivity :
    AppCompatActivity(),
    OnNavigationItemSelectedListener,
    AdItemClickListener,
    SearchUi,
    SearchActions,
    VoiceRecognitionListener,
    AccountHelperProvider,
    DialogHelperProvider,
    UserUiUpdate {
    private lateinit var textViewAccount: TextView
    private lateinit var imageViewAccount: ImageView

    private lateinit var binding: ActivityMainBinding
    private lateinit var orderByFilterDialogManager: OrderByFilterDialogManager

    private val viewModel: FirebaseViewModel by viewModels()

    private val onItemSelectedListener: RcViewSearchSpinnerAdapter.OnItemSelectedListener? = null
    private var adapterSearch = RcViewSearchSpinnerAdapter(onItemSelectedListener)

    private var lastClickTime: Long = 0
    private val doubleClickThreshold = 300

    private lateinit var voiceRecognitionHandler: VoiceRecognitionHandler
    private lateinit var voiceRecognitionLauncher: ActivityResultLauncher<Intent>

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

    private val searchHelper by lazy {
        SearchHelper(this, this)
    }

    private lateinit var accountHelper: AccountHelper

    private val googleSignInLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            accountHelper.handleGoogleSignInResult(result)
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(ThemeManager.getSelectedTheme(PreferenceManager.getDefaultSharedPreferences(this)))
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gso =
            GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getStringAccountHelper(R.string.default_web_client_id))
                .requestEmail()
                .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        accountHelper =
            AccountHelper(
                this,
                googleSignInClient,
                this,
            )

        textViewAccount = binding.navigationView.getHeaderView(0).findViewById(R.id.text_view_account_email)
        imageViewAccount = binding.navigationView.getHeaderView(0).findViewById(R.id.image_view_account_image)

        voiceRecognitionLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                voiceRecognitionHandler.handleRecognitionResult(result)
            }
        voiceRecognitionHandler = VoiceRecognitionHandler(this, voiceRecognitionLauncher)

        PermissionManager.checkAndRequestNotificationPermission(this)

        initRecyclerView()
        initViewModel()
        init()

        orderByFilterDialogManager = OrderByFilterDialogManager(this, viewModel)
        orderByFilterDialogManager.setupOrderByFilter(binding.mainContent.autoComplete)
        searchHelper.initSearchAdd()
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
                voiceRecognitionHandler.startVoiceRecognition()
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
        updateUi(viewModel.getAuth().currentUser)
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
            handleAdapterData(viewModel.favoriteAds, favAdsAdapter)
            handleAdapterData(viewModel.homeAdsData, adsAdapter)
            handleAdapterData(viewModel.myAds, myAdsAdapter)
        }
    }

    private fun updateUi(user: FirebaseUser?) {
        if (user == null) {
            accountHelper.signInAnonymously {
                textViewAccount.text = this.getString(R.string.guest)
                imageViewAccount.setImageResource(R.drawable.ic_my_ads)
            }
        } else if (user.isAnonymous) {
            textViewAccount.text = this.getString(R.string.guest)
            imageViewAccount.setImageResource(R.drawable.ic_my_ads)
        } else if (!user.isAnonymous) {
            textViewAccount.text = user.email
            Glide // meibe
                .with(this)
                .load(user.photoUrl)
                .apply(RequestOptions().transform(RoundedCorners(20)))
                .into(imageViewAccount)
        }
    }

    private fun handleAdapterData(
        dataFlow: Flow<PagingData<Ad>>,
        adapter: PagingDataAdapter<Ad, *>,
    ) {
        lifecycleScope.launch {
            dataFlow
                .catch { e ->
                    Timber.tag("MainActivity").e(e, "Error loading ads data")
                }.collectLatest { pagingData ->
                    Timber.d("Received PagingData: $pagingData")
                    adapter.submitData(lifecycle, pagingData)

                    adapter.addLoadStateListener { loadStates ->
                        if (loadStates.refresh is LoadState.NotLoading) {
                            val layoutManager = binding.mainContent.recyclerViewMainContent.layoutManager
                            layoutManager?.scrollToPosition(0)
                        }
                    }

                    adapter.registerAdapterDataObserver(
                        object :
                            RecyclerView.AdapterDataObserver() {
                            override fun onItemRangeInserted(
                                positionStart: Int,
                                itemCount: Int,
                            ) {
                                super.onItemRangeInserted(positionStart, itemCount)
                                Timber.d("onItemRangeInserted $adapter")
                                updateAnimationVisibility(adapter.itemCount)
                            }

                            override fun onItemRangeRemoved(
                                positionStart: Int,
                                itemCount: Int,
                            ) {
                                super.onItemRangeRemoved(positionStart, itemCount)
                                Timber.d("onItemRangeRemoved $adapter")
                                updateAnimationVisibility(adapter.itemCount)
                            }

                            override fun onChanged() {
                                super.onChanged()
                                Timber.d("onChanged $adapter")
                                updateAnimationVisibility(adapter.itemCount)
                            }
                        },
                    )
                    Timber.d("After $adapter")
                    updateAnimationVisibility(adapter.itemCount)
                }
        }
    }

    private fun updateAnimationVisibility(itemCount: Int) {
        Timber.d("updateAnimationVisibility Item count: $itemCount")

        if (itemCount == 0) {
            binding.mainContent.nothinkWhiteAnim.visibility = View.VISIBLE
            binding.mainContent.nothinkWhiteAnim.repeatCount = LottieDrawable.INFINITE
            binding.mainContent.nothinkWhiteAnim.playAnimation()
        } else {
            binding.mainContent.nothinkWhiteAnim.cancelAnimation()
            binding.mainContent.nothinkWhiteAnim.visibility = View.GONE
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
            val currentAdapterType =
                when (binding.mainContent.bottomNavView.selectedItemId) {
                    R.id.id_home -> ADS_ADAPTER
                    R.id.id_favs -> FAV_ADAPTER
                    R.id.id_my_ads -> MY_ADAPTER
                    else -> ADS_ADAPTER
                }
            refreshAdapter(currentAdapterType)
            binding.mainContent.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun refreshAdapter(adapterType: Int) {
        when (adapterType) {
            ADS_ADAPTER -> adsAdapter.refresh()
            FAV_ADAPTER -> favAdsAdapter.refresh()
            MY_ADAPTER -> myAdsAdapter.refresh()
        }
    }

    private fun setupBottomMenu() {
        with(binding) {
            mainContent.bottomNavView.setOnItemSelectedListener { item ->
                val currentTime = System.currentTimeMillis()

                if (currentTime - lastClickTime <= doubleClickThreshold) {
                    refreshAdapter(item.itemId)
                }
                lastClickTime = currentTime

                when (item.itemId) {
                    R.id.id_settings -> {
                        startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                    }
                    R.id.id_my_ads -> {
                        switchAdapter(myAdsAdapter, 2)
                        viewModel.myAds
                    }
                    R.id.id_favs -> {
                        switchAdapter(favAdsAdapter, 1)
                        viewModel.favoriteAds
                    }
                    R.id.id_home -> {
                        switchAdapter(adsAdapter, 0)
                        getAdsFromCat("")
                    }
                }
                true
            }
        }
    }

    private fun initRecyclerView() {
        binding.apply {
            mainContent.recyclerViewMainContent.layoutManager =
                LinearLayoutManager(this@MainActivity)
            mainContent.recyclerViewMainContent.adapter = adsAdapter
        }

        val onItemSelectedListener =
            RcViewSearchSpinnerAdapter.OnItemSelectedListener { item ->
                binding.mainContent.searchViewMainContent.editText
                    .setText(item)
                binding.mainContent.searchViewMainContent.editText.setSelection(
                    binding.mainContent.searchViewMainContent.editText.text.length,
                )
            }
        adapterSearch = RcViewSearchSpinnerAdapter(onItemSelectedListener)

        binding.apply {
            mainContent.recyclerViewSearch.layoutManager = LinearLayoutManager(this@MainActivity)
            mainContent.recyclerViewSearch.adapter = adapterSearch
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.id_my_ads -> {
                Toast.makeText(this, "pressed my ads", Toast.LENGTH_SHORT).show()
            }

            R.id.id_car -> {
                getAdsFromCat(SortOption.AD_CAR.id)
            }

            R.id.id_pc -> {
                getAdsFromCat(SortOption.AD_PC.id)
            }

            R.id.id_smartphone -> {
                getAdsFromCat(SortOption.AD_SMARTPHONE.id)
            }

            R.id.id_dm -> {
                getAdsFromCat(SortOption.AD_DM.id)
            }

            R.id.id_sign_up -> {
                SignInDialogFragment(
                    googleSignInLauncher,
                    DialogConst.SIGN_UP_STATE,
                    accountHelper,
                    this,
                ).show(supportFragmentManager, "SignUpDialog")
            }

            R.id.id_sign_in -> {
                SignInDialogFragment(
                    googleSignInLauncher,
                    DialogConst.SIGN_IN_STATE,
                    accountHelper,
                    this,
                ).show(supportFragmentManager, "SignInDialog")
            }

            R.id.id_sign_out -> {
                if (viewModel.getAuth().currentUser?.isAnonymous == true) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    return true
                }
                updateUi(null)
                viewModel.getAuth().signOut()
                accountHelper.signOutGoogle()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun getAdsFromCat(cat: String) {
        val newFilters =
            mapOf(
                CATEGORY_FIELD to cat,
                ORDER_BY_FIELD to (viewModel.getFilterValue(ORDER_BY_FIELD) ?: SortOption.BY_NEWEST.id),
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

    private fun switchAdapter(
        adapter: RecyclerView.Adapter<*>,
        tabPosition: Int,
    ) {
        updateAnimationVisibility(adapter.itemCount)
        // Сохранение текущего состояния
        scrollStateMap[currentTabPosition] =
            binding.mainContent.recyclerViewMainContent.layoutManager
                ?.onSaveInstanceState()

        // Переключение адаптера
        binding.mainContent.recyclerViewMainContent.adapter = adapter

        // Восстановление состояния для новой вкладки
        binding.mainContent.recyclerViewMainContent.layoutManager
            ?.onRestoreInstanceState(scrollStateMap[tabPosition])

        // Обновляем текущую позицию вкладки
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

    override fun isOwner(adUid: String): Boolean = adUid == viewModel.getAuth().currentUser?.uid

    override fun clearSearchResults() {
        adapterSearch.clearAdapter()
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

    override fun addToFilter(
        key: String,
        value: String,
    ) {
        viewModel.addToFilter(key, value)
    }

    override fun handleSearchQuery(query: String) {
        lifecycleScope.launch {
            viewModel.fetchSearchResults(query)
            viewModel.appState.collectLatest { appState ->
                if (appState.searchResults.isNotEmpty()) {
                    val formattedResults = viewModel.formatSearchResults(appState.searchResults, query)
                    adapterSearch.updateAdapter(formattedResults)
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
        Timber.d("validate text = $validateText")
        viewModel.addToFilter(KEYWORDS_FIELD, validateText)
    }

    override fun onVoiceRecognitionError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun saveToken(token: String) {
        viewModel.viewModelScope.launch {
            viewModel.saveTokenFCM(token)
        }
    }

    override val mAuth: FirebaseAuth
        get() = viewModel.getAuth()

    override fun showToast(
        message: String,
        duration: Int,
    ) {
        Toast.makeText(this, message, duration).show()
    }

    override fun getStringAccountHelper(resId: Int): String = this.getString(resId)

    override val mAuthImpl: FirebaseAuth
        get() = viewModel.getAuth()

    override fun showToastImpl(
        message: String,
        duration: Int,
    ) {
        Toast.makeText(this, message, duration).show()
    }

    override fun updateUiImpl(user: FirebaseUser?) {
        updateUi(user)
    }
}
