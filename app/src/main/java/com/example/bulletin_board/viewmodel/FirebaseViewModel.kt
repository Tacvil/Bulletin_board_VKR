package com.example.bulletin_board.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bulletin_board.model.Announcement
import com.example.bulletin_board.model.DbManager

class FirebaseViewModel: ViewModel() {
    private val dbManager = DbManager()
    val liveAdsData = MutableLiveData<ArrayList<Announcement>?>()
    fun loadAllAnnouncement(){
        dbManager.getAllAnnouncement(object : DbManager.ReadDataCallback{
            override fun readData(list: ArrayList<Announcement>) {
                liveAdsData.value = list
            }
        })
    }

    fun loadMyAnnouncement(){
        dbManager.getMyAnnouncement(object : DbManager.ReadDataCallback{
            override fun readData(list: ArrayList<Announcement>) {
                liveAdsData.value = list
            }
        })
    }

    fun deleteItem(ad: Announcement){
        dbManager.deleteAnnouncement(ad, object: DbManager.FinishWorkListener{
            override fun onFinish() {
                val updatedList = liveAdsData.value
                updatedList?.remove(ad)
                liveAdsData.postValue(updatedList)
            }

        })
    }
}