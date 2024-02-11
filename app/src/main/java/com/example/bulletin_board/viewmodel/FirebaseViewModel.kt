package com.example.bulletin_board.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bulletin_board.model.Announcement
import com.example.bulletin_board.model.DbManager

class FirebaseViewModel: ViewModel() {
    private val dbManager = DbManager()
    val liveAdsData = MutableLiveData<ArrayList<Announcement>?>()
    fun loadAllAnnouncementFirstPage(filter: String){
        dbManager.getAllAnnouncementFirstPage1(filter, object : DbManager.ReadDataCallback{
            override fun readData(list: ArrayList<Announcement>) {
                liveAdsData.value = list
                Log.d("FBVM", "liveAdsData1: ${liveAdsData.value}")
            }
        })
    }

    fun loadAllAnnouncementNextPage(time: String, filter: String){
        dbManager.getAllAnnouncementNextPage1(time, filter, object : DbManager.ReadDataCallback{
            override fun readData(list: ArrayList<Announcement>) {
                liveAdsData.value = list
                Log.d("FBVM", "liveAdsData2: ${liveAdsData.value}")
            }
        })
    }

    fun loadAllAnnouncementFromCatFirstPage(cat: String, filter: String){
//        dbManager.getAllAnnouncementFromCatFirstPage(cat, filter, object : DbManager.ReadDataCallback{
//            override fun readData(list: ArrayList<Announcement>) {
//                liveAdsData.value = list
//            }
//        })
    }

    fun loadAllAnnouncementFromCatNextPage(cat: String, time: String, filter: String){
//        dbManager.getAllAnnouncementFromCatNextPage(cat, time, filter, object : DbManager.ReadDataCallback{
//            override fun readData(list: ArrayList<Announcement>) {
//                liveAdsData.value = list
//            }
//        })
    }

    fun onFavClick(ad: Announcement){
        dbManager.onFavClick(ad, object: DbManager.FinishWorkListener{
            override fun onFinish(isDone: Boolean) {
                val updateList = liveAdsData.value
                val pos = updateList?.indexOf(ad)

                if (pos != -1){
                    pos?.let {
                        val favCounter = if (ad.isFav) ad.favCounter.toInt() - 1 else ad.favCounter.toInt() + 1
                        updateList[pos] = updateList[pos].copy(isFav = !ad.isFav, favCounter = favCounter.toString())
                    }
                }
                liveAdsData.postValue(updateList)
            }
        })
    }

    fun adViewed(ad: Announcement){
        dbManager.adViewed(ad)
    }

    fun loadMyAnnouncement(){
//        dbManager.getMyAnnouncement(object : DbManager.ReadDataCallback{
//            override fun readData(list: ArrayList<Announcement>) {
//                liveAdsData.value = list
//            }
//        })
    }

    fun loadMyFavs(){
//        dbManager.getMyFavs(object : DbManager.ReadDataCallback{
//            override fun readData(list: ArrayList<Announcement>) {
//                liveAdsData.value = list
//            }
//        })
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
}