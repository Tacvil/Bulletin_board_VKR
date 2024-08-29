package com.example.bulletin_board.act

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
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
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.bulletin_board.R
import com.example.bulletin_board.accounthelper.AccountHelper
import com.example.bulletin_board.adapters.AdsRcAdapter
import com.example.bulletin_board.adapters.FavsAdapter
import com.example.bulletin_board.adapters.MyAdsAdapter
import com.example.bulletin_board.databinding.ActivityMainBinding
import com.example.bulletin_board.dialoghelper.DialogConst
import com.example.bulletin_board.dialoghelper.DialogHelper
import com.example.bulletin_board.dialogs.DialogSpinnerHelper
import com.example.bulletin_board.dialogs.RcViewDialogSpinnerAdapter
import com.example.bulletin_board.dialogs.RcViewSearchSpinnerAdapter
import com.example.bulletin_board.model.Announcement
import com.example.bulletin_board.model.DbManager
import com.example.bulletin_board.model.SortOption
import com.example.bulletin_board.settings.SettingsActivity
import com.example.bulletin_board.utils.BillingManager
import com.example.bulletin_board.utils.BillingManager.Companion.REMOVE_ADS_PREF
import com.example.bulletin_board.viewmodel.FirebaseViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.io.Serializable

class MainActivity :
    AppCompatActivity(),
    OnNavigationItemSelectedListener,
    AdsRcAdapter.Listener {
    private lateinit var textViewAccount: TextView
    private lateinit var imageViewAccount: ImageView
    private lateinit var binding: ActivityMainBinding
    // private val dialogHelper = DialogHelper(this)

    private val dialogHelper = DialogHelper(this) { AccountHelper(this) }

    private val dialog = DialogSpinnerHelper()
    val mAuth = Firebase.auth
    val homeAdsAdapter = AdsRcAdapter(this)
    private val myAdsAdapter = MyAdsAdapter(this)
    private val favsAdapter = FavsAdapter(this)
    lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private lateinit var filterLauncher: ActivityResultLauncher<Intent>
    private val firebaseViewModel: FirebaseViewModel by viewModels()
    private var clearUpdate: Boolean = false
    private var filterDb: MutableMap<String, String> = mutableMapOf()
    private var pref: SharedPreferences? = null
    private var isPremiumUser: Boolean = false
    private var bManager: BillingManager? = null
    private val onItemSelectedListener: RcViewSearchSpinnerAdapter.OnItemSelectedListener? = null
    private var adapterSearch = RcViewSearchSpinnerAdapter(onItemSelectedListener)
    private lateinit var defPreferences: SharedPreferences
    private var viewModelIsLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        defPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        setTheme(getSelectedTheme())
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        pref = getSharedPreferences(BillingManager.MAIN_PREF, MODE_PRIVATE)
        isPremiumUser = pref?.getBoolean(REMOVE_ADS_PREF, false)!!
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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Запрос разрешения
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), PERMISSION_REQUEST_CODE)
        } else {
            // Разрешение уже предоставлено, можно отправлять уведомления
            Log.d("POST_NOTIFICATIONS_PER_TRUE", "POST_NOTIFICATIONS_PER_TRUE")
        }

        init()
        onClickSelectOrderByFilter()
        searchAdd()
        initRecyclerView()
        initViewModel()
        bottomMenuOnClick()
        scrollListener()
        onActivityResultFilter()
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
                    Log.d("POST_NOTIFICATIONS_PER_TRUE", "POST_NOTIFICATIONS_PER_TRUE")
                } else {
                    Log.d("POST_NOTIFICATIONS_PER_FALSE", "POST_NOTIFICATIONS_PER_FALSE")
                    Toast.makeText(this, "Permission denied, notifications cannot be sent", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

    private fun searchAdd() {
        binding.mainContent.searchViewMainContent.editText.addTextChangedListener(
            object :
                TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int,
                ) {
                    var searchQuery = s.toString()
                    if (searchQuery.isEmpty()) {
                        adapterSearch.clearAdapter()
                        adapterSearch.setOnDataChangedListener {
                            adapterSearch.clearAdapter()
                        }
                    } else {
                        adapterSearch.setOnDataChangedListener {
                        }
                    }
                    Log.d(
                        "MActTextChanged",
                        "searchQuery = $searchQuery, isEmpty = ${searchQuery.isEmpty()}",
                    )
                    if (searchQuery.trim().isNotEmpty()) {
                        // Убираем пробелы в начале строки
                        searchQuery = searchQuery.trimStart()

                        // Убираем двойные, тройные и т.д. пробелы во всей строке
                        searchQuery = searchQuery.replace(Regex("\\s{2,}"), " ")
                        Log.d("MActTextChanged", "searchQueryAfterValid = $searchQuery}")

                        val db = FirebaseFirestore.getInstance()
                        val collectionReference = db.collection(DbManager.MAIN_NODE)
                        val query = collectionReference.whereGreaterThanOrEqualTo("title", searchQuery)
                        val spaceCount = searchQuery.count { it == ' ' }
                        Log.d("MActTextChanged", "spaceCount = $spaceCount")
                        val phraseBuilder = StringBuilder()
                        val results = mutableListOf<String>()
                        var pairsResultSearch: ArrayList<Pair<String, String>>
                        query.get().addOnSuccessListener { documents ->
                            for (document in documents) {
                                val title = document.getString("title") ?: ""
                                Log.d("MActTextChanged", "title = $title")
                                val words = title.split("\\s+".toRegex())
                                Log.d("MActTextChanged", "words = $words")
                                when {
                                    spaceCount == 0 -> {
                                        val phrase = words[spaceCount]
                                        Log.d("MActTextChanged", "phrase = $phrase")
                                        results.add(phrase)
                                        Log.d("MActTextChanged", "results = $results")
                                    }

                                    spaceCount > 0 -> {
                                        phraseBuilder
                                            .append(searchQuery.substringBeforeLast(' '))
                                            .append(" ")
                                        words.getOrNull(spaceCount)?.let {
                                            phraseBuilder.append(it)
                                        }
                                        val phrase = phraseBuilder.toString()
                                        phraseBuilder.clear()
                                        Log.d("MActTextChanged", "phrase = $phrase")
                                        results.add(phrase)
                                    }
                                }
                            }
                            pairsResultSearch = ArrayList(results.map { Pair(it, "search") })
                            Log.d("MActTextChanged", "pairsResultSearch = $pairsResultSearch")
                            adapterSearch.updateAdapter(pairsResultSearch)
                        }
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                }
            },
        )
        // Нажатие на ЛУПУ
        binding.mainContent.searchViewMainContent.editText.setOnEditorActionListener { v, actionId, event ->
            val querySearch: String =
                binding.mainContent.searchViewMainContent.text
                    .toString()
            if (querySearch.trim().isNotEmpty()) {
                binding.mainContent.searchBar.setText(querySearch)
                binding.mainContent.searchBar.menu
                    .findItem(R.id.id_search)
                    .setIcon(R.drawable.ic_cancel)

                val titleValidate = queryValidate(querySearch)

                filterDb["keyWords"] = titleValidate
                Log.d("MainActSearch", "filterDb = $filterDb")
                clearUpdate = true
                firebaseViewModel.loadAllAnnouncements(this@MainActivity, filterDb)
            }
            binding.mainContent.searchViewMainContent.hide()

            false
        }

        binding.mainContent.searchBar.setOnClickListener {
            val textSearchBar =
                binding.mainContent.searchBar.text
                    .toString()
            binding.mainContent.searchViewMainContent.editText
                .setText(textSearchBar)
        }
        binding.mainContent.searchViewMainContent.toolbar.setOnClickListener {
            Log.d("MenuClick", "CLICK - $it")
        }
    }

    private fun queryValidate(query: String): String {
        val validateData = query.split(" ").joinToString("-")
        Log.d("MainActQueryValidate", "validateData = $validateData")
        return validateData
    }

    private fun onClickSelectOrderByFilter() =
        with(binding) {
            mainContent.autoComplete.setText(filterDb["orderBy"])
            mainContent.autoComplete.setOnClickListener {
                val listVariant: ArrayList<Pair<String, String>> =
                    if (filterDb["price_from"]?.isNotEmpty() == true || filterDb["price_to"]?.isNotEmpty() == true) {
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
                            Toast.makeText(this@MainActivity, "Item: $item", Toast.LENGTH_SHORT).show()
                            mainContent.autoComplete.setText(item)

                            filterDb["orderBy"] = getSortOption(item)
                            Log.d("MainActOnClickFilter", "filterDb = $filterDb")
                            clearUpdate = true
                            firebaseViewModel.loadAllAnnouncements(this@MainActivity, filterDb)
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
                val i =
                    Intent(this@MainActivity, FilterActivity::class.java).apply {
                        putExtra(FilterActivity.FILTER_KEY, filterDb as Serializable)
                    }
                filterLauncher.launch(i)
            }
        }

    private fun getSortOption(item: String): String =
        when (item) {
            getString(R.string.sort_by_newest) -> SortOption.BY_NEWEST.id
            getString(R.string.sort_by_popularity) -> SortOption.BY_POPULARITY.id
            getString(R.string.sort_by_ascending_price) -> SortOption.BY_PRICE_ASC.id
            getString(R.string.sort_by_descending_price) -> SortOption.BY_PRICE_DESC.id
            else -> item
        }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d("MenuClick", "CLICK - $item")
        when (item.itemId) {
            android.R.id.home -> {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }

            R.id.id_search -> {
                if (!filterDb["keyWords"].isNullOrEmpty()) {
                    // Текущая иконка НЕ является ic_search
                    binding.mainContent.searchBar.clearText()
                    filterDb["keyWords"] = ""
                    Log.d("MainActR.id.id_search", "filterDb = $filterDb")
                    clearUpdate = true
                    firebaseViewModel.loadAllAnnouncements(this@MainActivity, filterDb)
                    item.setIcon(R.drawable.ic_search)
                } else {
                    binding.mainContent.searchBar.performClick()
                }
                return true
            }

            R.id.id_voice -> {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
                )
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak something")

                try {
                    voiceRecognitionLauncher.launch(intent)
                } catch (e: ActivityNotFoundException) {
                    // Обработка ситуации, когда нет подходящей активности
                    Toast
                        .makeText(
                            this,
                            "Голосовое распознавание не поддерживается на вашем устройстве",
                            Toast.LENGTH_SHORT,
                        ).show()
                }
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private val voiceRecognitionLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)

                if (!results.isNullOrEmpty()) {
                    val spokenText = results[0]
                    Log.d("VoiceSearch", "Распознанный текст: $spokenText")

                    binding.mainContent.searchBar.setText(spokenText)
                    binding.mainContent.searchBar.menu
                        .findItem(R.id.id_search)
                        .setIcon(R.drawable.ic_cancel)

                    val validateText = queryValidate(spokenText)

                    filterDb["keyWords"] = validateText
                    Log.d("MainActSpokenText", "filterDb = $filterDb")
                    clearUpdate = true
                    firebaseViewModel.loadAllAnnouncements(this@MainActivity, filterDb)
                } else {
                    Log.d("VoiceSearch", "Распознавание речи не дало результатов.")
                }
            }
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
                    Log.d("MyLog", "Api exception: ${e.message} ")
                }
            }
    }

    private fun onActivityResultFilter() {
        filterLauncher =
            registerForActivityResult(
                ActivityResultContracts.StartActivityForResult(),
            ) {
                if (it.resultCode == RESULT_OK) {
                    Log.d("MyLogMainAct", "filterDbBefore: $filterDb")
                    val newFilterData = (it.data?.getSerializableExtra(FilterActivity.FILTER_KEY) as? MutableMap<String, String>)!!
                    filterDb.putAll(newFilterData)
                    if (filterDb["price_from"]?.isNotEmpty() == true || filterDb["price_to"]?.isNotEmpty() == true) {
                        filterDb["orderBy"] = "По возрастанию цены"
                        binding.mainContent.autoComplete.setText(filterDb["orderBy"])
                    }
                    Log.d("MyLogMainAct", "filterDb: $filterDb")
                    // filterDb = FilterManager.getFilter(filter)
                } else if (it.resultCode == RESULT_CANCELED) {
                    filterDb = mutableMapOf()
                }
            }
    }

    override fun onStart() {
        super.onStart()
        uiUpdate(mAuth.currentUser)
    }

    private fun initViewModel() {
        firebaseViewModel.isLoading.observe(this) { isLoading ->
            viewModelIsLoading = isLoading
        }
        firebaseViewModel.myAdsData.observe(this) { list ->
            list?.let { myAdsAdapter.updateAdapter(it) } // Обновляем адаптер
        }
        firebaseViewModel.favsData.observe(this) { list ->
            list?.let { favsAdapter.updateAdapter(it) } // Обновляем адаптер
        }
        firebaseViewModel.homeAdsData.observe(this) {
            it?.let { content ->
                // val list = getAdsByCategory(content)
                Log.d("MainActInitViewModel", "clearUpdate: $clearUpdate")

                if (!clearUpdate) {
                    homeAdsAdapter.updateAdapter(content)
                } else {
                    homeAdsAdapter.updateAdapterWithClear(content)
                }

                if (homeAdsAdapter.itemCount == 0) {
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

    private fun init() {
        filterDb["category"] = getString(R.string.def)
        filterDb["orderBy"] = getString(R.string.sort_by_newest)
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
    }

    private fun bottomMenuOnClick() =
        with(binding) {
            mainContent.floatingActButton.setOnClickListener {
                if (mAuth.currentUser?.isAnonymous == true) {
                    Toast
                        .makeText(
                            this@MainActivity,
                            "Чтобы создавать объявления - зарегистрируйтесь!",
                            Toast.LENGTH_SHORT,
                        ).show()
                } else {
                    val i = Intent(this@MainActivity, EditAdsActivity::class.java)
                    startActivity(i)
                }
            }

            mainContent.bottomNavView.setOnItemSelectedListener { item ->
                clearUpdate = false
                when (item.itemId) {
                    R.id.id_settings -> {
                        val i = Intent(this@MainActivity, SettingsActivity::class.java)
                        startActivity(i)
                    }

                    R.id.id_my_ads -> {
                        firebaseViewModel.loadMyAnnouncement()
                        binding.mainContent.recyclerViewMainContent.adapter = myAdsAdapter
                        // mainContent.toolbar.title = getString(R.string.ad_my_ads)
                    }

                    R.id.id_favs -> {
                        firebaseViewModel.loadMyFavs()
                        binding.mainContent.recyclerViewMainContent.adapter = favsAdapter
                        // mainContent.toolbar.title = getString(R.string.favs)
                    }

                    R.id.id_home -> {
                        binding.mainContent.recyclerViewMainContent.adapter = homeAdsAdapter
                        getAdsFromCat(getString(R.string.def))
                    }
                }
                true
            }
        }

    private fun initRecyclerView() {
        binding.apply {
            mainContent.recyclerViewMainContent.layoutManager =
                LinearLayoutManager(this@MainActivity)
            mainContent.recyclerViewMainContent.adapter = homeAdsAdapter
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

    private fun getAdsFromCat(cat: String) {
        filterDb["category"] = cat
        Log.d("MainActivityCAT", "firebaseViewModel.homeAdsData.value = ${firebaseViewModel.homeAdsData.value}")
        Log.d("MainActivityCAT", "_isLoading.value = $viewModelIsLoading")
        filterDb["orderBy"] = filterDb["orderBy"]?.let { getSortOption(it) }.toString()
        Log.d("MainActivityCAT", "filterDb = $filterDb")

        firebaseViewModel.loadAllAnnouncements(this, filterDb)
    }

    fun uiUpdate(user: FirebaseUser?) {
        if (user == null) {
            dialogHelper.accHelper.signInAnonymously(
                object : AccountHelper.Listener {
                    override fun onComplete() {
                        textViewAccount.text = "Гость"
                        imageViewAccount.setImageResource(R.drawable.ic_my_ads)
                    }
                },
            )
        } else if (user.isAnonymous) {
            textViewAccount.text = "Гость"
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

    override fun onDeleteItem(ad: Announcement) {
        firebaseViewModel.deleteItem(ad)
        clearUpdate = true
    }

    override fun onAdViewed(ad: Announcement) {
        firebaseViewModel.adViewed(ad)
        val i = Intent(this, DescriptionActivity::class.java)
        i.putExtra("AD", ad)
        startActivity(i)
    }

    override fun onFavClicked(
        ad: Announcement,
        adArray: ArrayList<Announcement>,
    ) {
        clearUpdate = true
        firebaseViewModel.onFavClick(ad, adArray)
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

    private fun scrollListener() =
        with(binding.mainContent) {
            recyclerViewMainContent.addOnScrollListener(
                object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(
                        recyclerView: RecyclerView,
                        newState: Int,
                    ) {
                        super.onScrollStateChanged(recyclerView, newState)
                        if (!recyclerView.canScrollVertically(SCROLL_DOWN) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                            if (!viewModelIsLoading) {
                                clearUpdate = false
                                val adsList = firebaseViewModel.homeAdsData.value
                                Log.d("MainAct_scrollListener", "adsList: $adsList")
                                if (adsList != null && adsList.isNotEmpty()) {
                                    getAdsFromCat(adsList)
                                }
                            }
                        }
                    }
                },
            )
        }

    private fun getAdsFromCat(adsList: List<Announcement>) {
        adsList.lastOrNull()?.let {
            firebaseViewModel.loadAllAnnouncements(this, filterDb)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bManager?.closeConnection()
        firebaseViewModel.clearCache()
    }

    private fun getSelectedTheme(): Int =
        when (defPreferences.getString(SettingsActivity.THEME_KEY, SettingsActivity.DEFAULT_THEME)) {
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
    }
}
