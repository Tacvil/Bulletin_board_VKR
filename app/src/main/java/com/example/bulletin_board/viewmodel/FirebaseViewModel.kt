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

@OptIn(FlowPreview::class)
class FirebaseViewModel : ViewModel() {
    private val dbManager = DbManager()
    val homeAdsData = MutableLiveData<List<Announcement>?>(emptyList())
    val myAdsData = MutableLiveData<List<Announcement>?>(emptyList())
    val favsData = MutableLiveData<List<Announcement>?>(emptyList())
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
                                            homeAdsData.value = list
                                            lastDocumentAds = lastDocument
                                            Log.d("FirebaseViewModel", "Список объявлений: $list")
                                        }

                                        override fun onComplete() {
                                            _isLoading.value = false
                                        }

                                        override fun onError(e: Exception) {
                                            Log.e("FirebaseViewModel", "Ошибка загрузки данных", e)
                                            homeAdsData.postValue(null)
                                        }
                                    },
                            )
                        } catch (e: Exception) {
                            Log.e("FirebaseViewModel", "Ошибка загрузки данных", e)
                            homeAdsData.postValue(null)
                            _isLoading.value = false
                        }
                    }
            }
        }
    }

    fun clearCache() {
        homeAdsData.value = emptyList()
        lastDocumentAds = null
    }

    fun loadAllAnnouncements(
        context: Context,
        filter: MutableMap<String, String>,
    ) {
        if (_isLoading.value == true) return
        Log.d("loadAllAnnouncements", "loadAllAnnouncements")
        viewModelScope.launch {
            loadAllAnnouncementsChannel.send(Pair(context, filter))
        }
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
                    homeAdsData.postValue(adArray)
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
                    myAdsData.value = list
                }

                override fun onComplete() {
                    _isLoading.value = false
                }

                override fun onError(e: Exception) {
                    Log.e("FirebaseViewModel", "Ошибка загрузки данных", e)
                    myAdsData.postValue(null)
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
                    favsData.value = list
                }

                override fun onComplete() {
                    _isLoading.value = false
                }

                override fun onError(e: Exception) {
                    Log.e("FirebaseViewModel", "Ошибка загрузки данных", e)
                    favsData.postValue(null)
                }
            },
        )
    }

    fun deleteItem(ad: Announcement) {
        dbManager.deleteAnnouncement(
            ad,
            object : DbManager.FinishWorkListener {
                override fun onFinish(isDone: Boolean) {
                    val updatedList = homeAdsData.value?.toMutableList() // Создаем изменяемый список
                    updatedList?.remove(ad)
                    homeAdsData.postValue(updatedList)
                }
            },
        )
    }

    fun saveTokenDB(token: String) {
        dbManager.saveToken(token)
    }
}
