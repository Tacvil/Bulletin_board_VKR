package com.example.bulletin_board.model

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class DbManager {
    val database = Firebase.database.getReference("main")
    val auth = Firebase.auth

    fun publishAnnouncement(announcement: Announcement){
        if (auth.uid != null)database.child(announcement.key ?: "empty").child(auth.uid!!).child("announcement").setValue(announcement)
    }

    fun readDataFromDb(readDataCallback: ReadDataCallback?){
        database.addListenerForSingleValueEvent(object : ValueEventListener{
            val adArray = ArrayList<Announcement>()
            override fun onDataChange(snapshot: DataSnapshot) {
                for (item in snapshot.children){
                    val ad = item.children.iterator().next().child("announcement").getValue(Announcement::class.java)
                    Log.d("MyLog", "data: $ad")
                    if (ad != null)adArray.add(ad)
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

}