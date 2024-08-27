package com.example.bulletin_board.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bulletin_board.model.Announcement
import com.example.bulletin_board.model.DbManager
import com.google.firebase.firestore.QueryDocumentSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.debounce

@OptIn(FlowPreview::class)
class FirebaseViewModel : ViewModel() {
    private val dbManager = DbManager()
    val liveAdsData = MutableLiveData<ArrayList<Announcement>?>()
    private var currentSortOption: String? = null
    private var lastDocumentAds: QueryDocumentSnapshot? = null
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val loadAllAnnouncementsChannel = Channel<Pair<Context, MutableMap<String, String>>>(Channel.CONFLATED)
    private var currentJob: Job? = null

    init {
        viewModelScope.launch {
            loadAllAnnouncementsChannel.consumeAsFlow().debounce(300).collect { (context, filter) ->
                currentJob?.cancel()
                currentJob =
                    viewModelScope.launch(Dispatchers.IO) {
                        _isLoading.postValue(true)
                        try {
                            if (currentSortOption != filter["orderBy"]) {
                                lastDocumentAds = null
                                currentSortOption = filter["orderBy"]
                            }
                            dbManager.getAllAnnouncements(
                                context = context,
                                filter = filter,
                                lastDocumentAds = lastDocumentAds,
                                readDataCallback =
                                    object : DbManager.ReadDataCallback {
                                        override fun readData(
                                            list: ArrayList<Announcement>,
                                            lastDocument: QueryDocumentSnapshot?,
                                        ) {
                                            liveAdsData.postValue(list)
                                            lastDocumentAds = lastDocument
                                        }

                                        override fun onError(e: Exception) {
                                            Log.e("FirebaseViewModel", "Ошибка загрузки данных", e)
                                            liveAdsData.postValue(null)
                                        }
                                    },
                                onComplete = { _isLoading.value = false },
                            )
                        } catch (e: Exception) {
                            Log.e("FirebaseViewModel", "Ошибка загрузки данных", e)
                            liveAdsData.postValue(null)
                            _isLoading.value = false
                        }
                    }
            }
        }
    }

    fun loadAllAnnouncements(
        context: Context,
        filter: MutableMap<String, String>,
    ) {
        if (_isLoading.value == true) return
        viewModelScope.launch {
            loadAllAnnouncementsChannel.send(Pair(context, filter))
        }
    }

 /*   fun loadAllAnnouncementFirstPage(
        context: Context,
        filter: MutableMap<String, String>,
    ) {
        dbManager.getAllAnnouncementFirstPage1(
            context,
            filter,
            object : DbManager.ReadDataCallback {
                override fun readData(
                    list: ArrayList<Announcement>,
                    lastDocument: QueryDocumentSnapshot?,
                ) {
                    liveAdsData.value = list
                    lastDocumentAds = lastDocument
                    Log.d("FBVM", "liveAdsData1: ${liveAdsData.value}")
                }
            },
        )
    }

    fun loadAllAnnouncementNextPage(
        context: Context,
        time: String,
        price: Int?,
        viewsCounter: Int,
        filter: MutableMap<String, String>,
        onComplete: () -> Unit,
    ) {
        dbManager.getAllAnnouncementNextPage1(
            context,
            time,
            price,
            viewsCounter,
            lastDocumentAds,
            filter,
            object : DbManager.ReadDataCallback {
                override fun readData(
                    list: ArrayList<Announcement>,
                    lastDocument: QueryDocumentSnapshot?,
                ) {
                    liveAdsData.value = list
                    lastDocumentAds = lastDocument
                    Log.d("FBVM", "liveAdsData2: ${liveAdsData.value}")
                    onComplete() // Вызов завершения
                }
            },
            onComplete, // Передача onComplete в getAllAnnouncementNextPage1
        )
    }*/

    fun loadAllAnnouncementFromCatFirstPage(filter: MutableMap<String, String>) {
//        dbManager.getAllAnnouncementFromCatFirstPage(cat, filter, object : DbManager.ReadDataCallback{
//            override fun readData(list: ArrayList<Announcement>) {
//                liveAdsData.value = list
//            }
//        })
    }

    fun loadAllAnnouncementFromCatNextPage(
        cat: String,
        time: String,
        filter: MutableMap<String, String>,
    ) {
//        dbManager.getAllAnnouncementFromCatNextPage(cat, time, filter, object : DbManager.ReadDataCallback{
//            override fun readData(list: ArrayList<Announcement>) {
//                liveAdsData.value = list
//            }
//        })
    }

    fun onFavClick(
        ad: Announcement,
        adArray: ArrayList<Announcement>,
    ) {
        dbManager.onFavClick(
            ad,
            object : DbManager.FinishWorkListener {
                override fun onFinish(isDone: Boolean) {
                    Log.d("updateList", "updateList = $adArray")
                    val pos = adArray.indexOf(ad)
                    Log.d("ViewModelFav", "pos = $pos")

                    if (pos != -1) {
                        pos.let {
                            val favCounter =
                                if (ad.isFav) ad.favCounter.toInt() - 1 else ad.favCounter.toInt() + 1
                            Log.d("ViewModelFav", "favCounter = $favCounter")
                            adArray[pos] =
                                adArray[pos].copy(
                                    isFav = !ad.isFav,
                                    favCounter = favCounter.toString(),
                                )
                            Log.d("ViewModelFav", "updateList[pos] = ${adArray[pos]}")
                        }
                    }
                    liveAdsData.postValue(adArray)
                }
            },
        )
    }

    fun adViewed(ad: Announcement) {
        dbManager.adViewed(ad)
    }

    fun loadMyAnnouncement() {
        dbManager.getMyAnnouncement(
            object : DbManager.ReadDataCallback {
                override fun readData(
                    list: ArrayList<Announcement>,
                    lastDocument: QueryDocumentSnapshot?,
                ) {
                    liveAdsData.value = list
                }

                override fun onError(e: Exception) {
                    Log.e("FirebaseViewModel", "Ошибка загрузки данных", e)
                    liveAdsData.postValue(null)
                }
            },
        )
    }

    fun loadMyFavs() {
        dbManager.getMyFavs(
            object : DbManager.ReadDataCallback {
                override fun readData(
                    list: ArrayList<Announcement>,
                    lastDocument: QueryDocumentSnapshot?,
                ) {
                    liveAdsData.value = list
                }

                override fun onError(e: Exception) {
                    Log.e("FirebaseViewModel", "Ошибка загрузки данных", e)
                    liveAdsData.postValue(null)
                }
            },
        )
    }

    fun deleteItem(ad: Announcement) {
        dbManager.deleteAnnouncement(
            ad,
            object : DbManager.FinishWorkListener {
                override fun onFinish(isDone: Boolean) {
                    val updatedList = liveAdsData.value
                    updatedList?.remove(ad)
                    liveAdsData.postValue(updatedList)
                }
            },
        )
    }

    fun saveTokenDB(token: String) {
        dbManager.saveToken(token)
    }
}
