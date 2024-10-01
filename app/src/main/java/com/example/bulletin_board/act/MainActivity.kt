package com.example.bulletin_board.act

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.example.bulletin_board.adapterFirestore.AdsAdapter
import com.example.bulletin_board.adapterFirestore.FavoriteAdsAdapter
import com.example.bulletin_board.adapterFirestore.MyAdsAdapter
import com.example.bulletin_board.databinding.ActivityMainBinding
import com.example.bulletin_board.dialoghelper.DialogConst
import com.example.bulletin_board.dialoghelper.DialogHelper
import com.example.bulletin_board.dialogs.DialogSpinnerHelper
import com.example.bulletin_board.dialogs.RcViewDialogSpinnerAdapter
import com.example.bulletin_board.dialogs.RcViewSearchSpinnerAdapter
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
import com.example.bulletin_board.utils.BillingManager
import com.example.bulletin_board.utils.SearchActions
import com.example.bulletin_board.utils.SearchHelper
import com.example.bulletin_board.utils.SearchUi
import com.example.bulletin_board.utils.SortUtils.getSortOption
import com.example.bulletin_board.utils.SortUtils.getSortOptionText
import com.example.bulletin_board.viewmodel.FirebaseViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
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
    VoiceRecognitionListener {
    private lateinit var textViewAccount: TextView
    private lateinit var imageViewAccount: ImageView
    private lateinit var binding: ActivityMainBinding
    private val dialogHelper = DialogHelper(this) { AccountHelper(this) }
    private val dialog = DialogSpinnerHelper()
    val mAuth = Firebase.auth
    lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    val viewModel: FirebaseViewModel by viewModels()

    private var bManager: BillingManager? = null

    private val onItemSelectedListener: RcViewSearchSpinnerAdapter.OnItemSelectedListener? = null
    private var adapterSearch = RcViewSearchSpinnerAdapter(onItemSelectedListener)

    private lateinit var defPreferences: SharedPreferences
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

    override fun onCreate(savedInstanceState: Bundle?) {
        defPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        setTheme(getSelectedTheme())
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        val toolbar: Toolbar = findViewById(R.id.toolbar)
//        setSupportActionBar(toolbar)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel.
            val name = getString(R.string.ad_car)
            val descriptionText = getString(R.string.ad_car)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel("123", name, importance)
            mChannel.description = descriptionText

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }

        voiceRecognitionLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                voiceRecognitionHandler.handleRecognitionResult(result)
            }
        voiceRecognitionHandler = VoiceRecognitionHandler(this, voiceRecognitionLauncher)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Запрос разрешения
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                PERMISSION_REQUEST_CODE,
            )
        } else {
            // Разрешение уже предоставлено, можно отправлять уведомления
            Timber.tag("POST_NOTIFICATIONS_PER_TRUE").d("POST_NOTIFICATIONS_PER_TRUE")
        }

        initRecyclerView()
        initViewModel()
        init()
        onClickSelectOrderByFilter()
        searchHelper.initSearchAdd()
        setupBottomMenu()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Разрешение предоставлено, можно отправлять уведомления
                    Timber.tag("POST_NOTIFICATIONS_PER_TRUE").d("POST_NOTIFICATIONS_PER_TRUE")
                } else {
                    Timber.tag("POST_NOTIFICATIONS_PER_FALSE").d("POST_NOTIFICATIONS_PER_FALSE")
                    Toast
                        .makeText(
                            this,
                            "Permission denied, notifications cannot be sent",
                            Toast.LENGTH_SHORT,
                        ).show()
                }
                return
            }
        }
    }

    private fun onClickSelectOrderByFilter() =
        with(binding) {
            mainContent.autoComplete.setOnClickListener {
                val listVariant: ArrayList<Pair<String, String>> =
                    if (viewModel
                            .getFilterValue("price_from")
                            ?.isNotEmpty() == true ||
                        viewModel
                            .getFilterValue("price_to")
                            ?.isNotEmpty() == true
                    ) {
                        arrayListOf(
                            Pair(getString(R.string.sort_by_ascending_price), "single"),
                            Pair(getString(R.string.sort_by_descending_price), "single"),
                        )
                    } else {
                        arrayListOf(
                            Pair(getString(R.string.sort_by_newest), "single"),
                            Pair(getString(R.string.sort_by_popularity), "single"),
                            Pair(getString(R.string.sort_by_ascending_price), "single"),
                            Pair(getString(R.string.sort_by_descending_price), "single"),
                        )
                    }
                val onItemSelectedListener =
                    object : RcViewDialogSpinnerAdapter.OnItemSelectedListener {
                        override fun onItemSelected(item: String) {
                            Toast
                                .makeText(this@MainActivity, "Item: $item", Toast.LENGTH_SHORT)
                                .show()

                            viewModel.addToFilter("orderBy", getSortOption(this@MainActivity, item))
                        }
                    }

                dialog.showSpinnerPopup(
                    this@MainActivity,
                    mainContent.autoComplete,
                    listVariant,
                    mainContent.autoComplete,
                    onItemSelectedListener,
                    false,
                )
            }

            mainContent.filterButtonMain.setOnClickListener {
                if (!filterFragment.isAdded) {
                    filterFragment.show(supportFragmentManager, FilterFragment.TAG)
                }
            }
        }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Timber.tag("MenuClick").d("CLICK - " + item)
        when (item.itemId) {
            android.R.id.home -> {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }

            R.id.id_search -> {
                if (!viewModel.getFilterValue("keyWords").isNullOrEmpty()) {
                    // Текущая иконка НЕ является ic_search
                    binding.mainContent.searchBar.clearText()
                    viewModel.addToFilter("keyWords", "")
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

    private fun onActivityResult() {
        googleSignInLauncher =
            registerForActivityResult(
                ActivityResultContracts.StartActivityForResult(),
            ) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)

                try {
                    val account = task.getResult(ApiException::class.java)
                    if (account != null) {
                        dialogHelper.accHelper.signInFirebaseWithGoogle(account.idToken!!)
                    }
                } catch (e: ApiException) {
                    Toast.makeText(this, "Api exception: ${e.message}", Toast.LENGTH_SHORT).show()
                    Timber.tag("MyLog").d("Api exception: " + e.message + " ")
                }
            }
    }

    override fun onStart() {
        super.onStart()
        uiUpdate(mAuth.currentUser)
    }

    private fun initViewModel() {
        lifecycleScope.launch {
            viewModel.appState.collectLatest { event ->
                if (event.filter.isNotEmpty()) {
                    val sortOptionId = event.filter[ORDER_BY_FIELD] ?: return@collectLatest
                    val sortOptionText = getSortOptionText(this@MainActivity, sortOptionId)
                    binding.mainContent.autoComplete.setText(sortOptionText)
                    Timber.d("MainActivity filter changed orderBy : $sortOptionText")
                }
            }
        }

        lifecycleScope.launch {
            handleAdapterData(viewModel.favoriteAds, favAdsAdapter)
            handleAdapterData(viewModel.homeAdsData, adsAdapter)
            handleAdapterData(viewModel.myAds, myAdsAdapter)
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
            // Показать анимацию
            binding.mainContent.nothinkWhiteAnim.visibility = View.VISIBLE
            binding.mainContent.nothinkWhiteAnim.repeatCount = LottieDrawable.INFINITE
            binding.mainContent.nothinkWhiteAnim.playAnimation()
        } else {
            // Скрыть анимацию
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
        onActivityResult()
        navViewSetting()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.navigationView.setNavigationItemSelectedListener(this)
        textViewAccount =
            binding.navigationView.getHeaderView(0).findViewById(R.id.text_view_account_email)
        imageViewAccount =
            binding.navigationView.getHeaderView(0).findViewById(R.id.image_view_account_image)

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
        Timber.d("refreshAdapter() called with: adapterType = $adapterType")
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
                    // Двойной клик
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
            object : RcViewSearchSpinnerAdapter.OnItemSelectedListener {
                override fun onItemSelected(item: String) {
                    Toast.makeText(this@MainActivity, "Item: $item", Toast.LENGTH_SHORT).show()
                    binding.mainContent.searchViewMainContent.editText
                        .setText(item)
                    binding.mainContent.searchViewMainContent.editText.setSelection(
                        binding.mainContent.searchViewMainContent.editText.text.length,
                    )
                }
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

    private fun getAdsFromCat(cat: String) {
        val newFilters =
            mapOf(
                CATEGORY_FIELD to cat,
                ORDER_BY_FIELD to (viewModel.getFilterValue(ORDER_BY_FIELD) ?: SortOption.BY_NEWEST.id),
            )
        Timber.d("newFilters called getAdsFromCat")
        viewModel.updateFilters(newFilters)
    }

    fun uiUpdate(user: FirebaseUser?) {
        if (user == null) {
            dialogHelper.accHelper.signInAnonymously(
                object : AccountHelper.Listener {
                    override fun onComplete() {
                        textViewAccount.text = getString(R.string.guest)
                        imageViewAccount.setImageResource(R.drawable.ic_my_ads)
                    }
                },
            )
        } else if (user.isAnonymous) {
            textViewAccount.text = getString(R.string.guest)
            imageViewAccount.setImageResource(R.drawable.ic_my_ads)
        } else if (!user.isAnonymous) {
            textViewAccount.text = user.email
            Glide
                .with(this)
                .load(user.photoUrl)
                .apply(RequestOptions().transform(RoundedCorners(20)))
                .into(imageViewAccount)
        }
    }

    private fun navViewSetting() =
        with(binding) {
            val menu = navigationView.menu
            val adsCat = menu.findItem(R.id.adsCat)
            val spanAdsCat = SpannableString(adsCat.title)

            val colorPrimary = R.color.md_theme_light_primary

            adsCat.title?.let {
                spanAdsCat.setSpan(
                    ForegroundColorSpan(
                        ContextCompat.getColor(
                            this@MainActivity,
                            colorPrimary,
                        ),
                    ),
                    0,
                    it.length,
                    0,
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
                            colorPrimary,
                        ),
                    ),
                    0,
                    it.length,
                    0,
                )
            }
            accCat.title = spanAccCat
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

    override fun onDestroy() {
        super.onDestroy()
        bManager?.closeConnection()
    }

    private fun getSelectedTheme(): Int =
        when (
            defPreferences.getString(
                SettingsActivity.THEME_KEY,
                SettingsActivity.DEFAULT_THEME,
            )
        ) {
            SettingsActivity.DEFAULT_THEME -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                R.style.Base_Theme_Bulletin_board_light
            }

            else -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                R.style.Base_Theme_Bulletin_board_dark
            }
        }

    companion object {
        const val EDIT_STATE = "edit_state"
        const val ADS_DATA = "ads_data"
        const val SCROLL_DOWN = 1
        const val REQUEST_CODE_SPEECH_INPUT = 100
        private const val PERMISSION_REQUEST_CODE = 1
        private const val ADS_ADAPTER = 0
        private const val FAV_ADAPTER = 1
        private const val MY_ADAPTER = 2
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

    override fun isOwner(adUid: String): Boolean = adUid == mAuth.currentUser?.uid

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

    private fun onVoiceButtonClick() {
        voiceRecognitionHandler.startVoiceRecognition()
    }
}
