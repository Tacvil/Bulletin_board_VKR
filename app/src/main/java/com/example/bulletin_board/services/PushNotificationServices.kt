package com.example.bulletin_board.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.activity.viewModels
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import com.example.bulletin_board.R
import com.example.bulletin_board.viewmodel.FirebaseViewModel
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PushNotificationServices:FirebaseMessagingService() {
    private lateinit var firebaseViewModel: FirebaseViewModel

    override fun onCreate() {
        super.onCreate()
        firebaseViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(application).create(FirebaseViewModel::class.java)

    }
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        firebaseViewModel.saveTokenDB(token)
        Log.d("NEW TOKEN", token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d("FROM SERVIS", "From: ${message.from}")

        // Check if message contains a data payload.
        if (message.data.isNotEmpty()) {
            Log.d("Message data payload SERVIS", "Message data payload: ${message.data}")

/*            // Check if data needs to be processed by long running job
            if (needsToBeScheduled()) {
                // For long-running tasks (10 seconds or more) use WorkManager.
                scheduleJob()
            } else {
                // Handle message within 10 seconds
                handleNow()
            }*/
        }

        // Check if message contains a notification payload.
        message.notification?.let {
            Log.d("NOTIFICATION SERVICE", "Message Notification Body: ${it.body}")
            Log.d("NOTIFICATION SERVICE2", "Message Notification title: ${it.title}")
            sendNotification(it.title ?: "", it.body ?: "")
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    private fun sendNotification(title: String, body: String) {
        val channelId = "Default"

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_favorite_pressed)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }

    companion object {
        private const val TAG = "PushNotificationService"
    }
}
