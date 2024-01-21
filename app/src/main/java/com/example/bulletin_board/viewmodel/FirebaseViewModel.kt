package com.example.bulletin_board.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bulletin_board.model.Announcement
import com.example.bulletin_board.model.DbManager

class FirebaseViewModel: ViewModel() {
    private val dbManager = DbManager()
    val liveAdsData = MutableLiveData<ArrayList<Announcement>?>()
    fun loadAllAnnouncement(lastTime: String){
        dbManager.getAllAnnouncement(lastTime, object : DbManager.ReadDataCallback{
            override fun readData(list: ArrayList<Announcement>) {
                liveAdsData.value = list
            }
        })
    }

    fun onFavClick(ad: Announcement){
        dbManager.onFavClick(ad, object: DbManager.FinishWorkListener{
            override fun onFinish() {
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
        dbManager.getMyAnnouncement(object : DbManager.ReadDataCallback{
            override fun readData(list: ArrayList<Announcement>) {
                liveAdsData.value = list
            }
        })
    }

    fun loadMyFavs(){
        dbManager.getMyFavs(object : DbManager.ReadDataCallback{
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