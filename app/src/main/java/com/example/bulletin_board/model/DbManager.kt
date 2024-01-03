package com.example.bulletin_board.model

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class DbManager {
    val database = Firebase.database.getReference("main")
    val auth = Firebase.auth

    fun publishAnnouncement(announcement: Announcement, finishListener:FinishWorkListener) {
        if (auth.uid != null) database.child(announcement.key ?: "empty").child(auth.uid!!)
            .child("announcement")
            .setValue(announcement).addOnCompleteListener {
                finishListener.onFinish()
            }
    }

    fun getMyAnnouncement(readDataCallback: ReadDataCallback?) {
        val query = database.orderByChild(auth.uid + "/announcement/uid").equalTo(auth.uid)
        readDataFromDb(query, readDataCallback)
    }

    fun getAllAnnouncement(readDataCallback: ReadDataCallback?) {
        val query = database.orderByChild(auth.uid + "/announcement/price")
        readDataFromDb(query, readDataCallback)
    }

    private fun readDataFromDb(query: Query, readDataCallback: ReadDataCallback?) {
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            val adArray = ArrayList<Announcement>()
            override fun onDataChange(snapshot: DataSnapshot) {
                for (item in snapshot.children) {
                    val ad = item.children.iterator().next().child("announcement")
                        .getValue(Announcement::class.java)
                    Log.d("MyLog", "data: $ad")
                    if (ad != null) adArray.add(ad)
                }
                readDataCallback?.readData(adArray)
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    interface ReadDataCallback {
        fun readData(list: ArrayList<Announcement>)
    }

    interface FinishWorkListener {
        fun onFinish()
    }
}