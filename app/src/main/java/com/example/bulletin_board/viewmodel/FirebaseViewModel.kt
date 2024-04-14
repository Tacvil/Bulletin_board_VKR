package com.example.bulletin_board.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bulletin_board.model.Announcement
import com.example.bulletin_board.model.DbManager
import com.google.firebase.firestore.QueryDocumentSnapshot

class FirebaseViewModel: ViewModel() {
    private val dbManager = DbManager()
    val liveAdsData = MutableLiveData<ArrayList<Announcement>?>()
    var lastDocumentAds: QueryDocumentSnapshot? = null
    fun loadAllAnnouncementFirstPage(context: Context, filter: MutableMap<String, String>){
        dbManager.getAllAnnouncementFirstPage1(context, filter, object : DbManager.ReadDataCallback{
            override fun readData(
                list: ArrayList<Announcement>,
                lastDocument: QueryDocumentSnapshot?
            ) {
                liveAdsData.value = list
                lastDocumentAds = lastDocument
                Log.d("FBVM", "liveAdsData1: ${liveAdsData.value}")
            }
        })
    }

    fun loadAllAnnouncementNextPage(context: Context, time: String, price: Int?, viewsCounter: String, filter: MutableMap<String, String>){
        dbManager.getAllAnnouncementNextPage1(context, time, price, viewsCounter, lastDocumentAds, filter, object : DbManager.ReadDataCallback{
            override fun readData(
                list: ArrayList<Announcement>,
                lastDocument: QueryDocumentSnapshot?
            ) {
                liveAdsData.value = list
                lastDocumentAds = lastDocument
                Log.d("FBVM", "liveAdsData2: ${liveAdsData.value}")
            }
        })
    }

    fun loadAllAnnouncementFromCatFirstPage(filter: MutableMap<String, String>){
//        dbManager.getAllAnnouncementFromCatFirstPage(cat, filter, object : DbManager.ReadDataCallback{
//            override fun readData(list: ArrayList<Announcement>) {
//                liveAdsData.value = list
//            }
//        })
    }

    fun loadAllAnnouncementFromCatNextPage(cat: String, time: String, filter: MutableMap<String, String>){
//        dbManager.getAllAnnouncementFromCatNextPage(cat, time, filter, object : DbManager.ReadDataCallback{
//            override fun readData(list: ArrayList<Announcement>) {
//                liveAdsData.value = list
//            }
//        })
    }

    fun onFavClick(
        ad: Announcement,
        adArray: ArrayList<Announcement>,
    ){
        dbManager.onFavClick(ad, object: DbManager.FinishWorkListener{
            override fun onFinish(isDone: Boolean) {
                Log.d("updateList", "updateList = $adArray")
                val pos = adArray.indexOf(ad)
                Log.d("ViewModelFav", "pos = $pos")

                if (pos != -1) {
                    pos.let {
                        val favCounter =
                            if (ad.isFav) ad.favCounter.toInt() - 1 else ad.favCounter.toInt() + 1
                        Log.d("ViewModelFav", "favCounter = $favCounter")
                        adArray[pos] = adArray[pos].copy(
                            isFav = !ad.isFav,
                            favCounter = favCounter.toString()
                        )
                        Log.d("ViewModelFav", "updateList[pos] = ${adArray[pos]}")
                    }
                }
                liveAdsData.postValue(adArray)
            }
        })
    }

    fun adViewed(ad: Announcement){
        dbManager.adViewed(ad)
    }

    fun loadMyAnnouncement(){
        dbManager.getMyAnnouncement(object : DbManager.ReadDataCallback{
            override fun readData(
                list: ArrayList<Announcement>,
                lastDocument: QueryDocumentSnapshot?
            ) {
                liveAdsData.value = list
            }
        })
    }

    fun loadMyFavs(){
        dbManager.getMyFavs(object : DbManager.ReadDataCallback{
            override fun readData(
                list: ArrayList<Announcement>,
                lastDocument: QueryDocumentSnapshot?
            ) {
                liveAdsData.value = list
            }
        })
    }

    fun deleteItem(ad: Announcement){
        dbManager.deleteAnnouncement(ad, object: DbManager.FinishWorkListener{
            override fun onFinish(isDone: Boolean) {
                val updatedList = liveAdsData.value
                updatedList?.remove(ad)
                liveAdsData.postValue(updatedList)
            }

        })
    }

    fun saveTokenDB(token: String){
        dbManager.saveToken(token)
    }
}