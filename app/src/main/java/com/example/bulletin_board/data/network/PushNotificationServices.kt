package com.example.bulletin_board.data.network

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.bulletin_board.R
import com.example.bulletin_board.application.AppApplication
import com.example.bulletin_board.domain.useCases.tokenManagement.SaveTokenUseCase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch

@EntryPoint
@InstallIn(SingletonComponent::class)
interface PushNotificationEntryPoint {
    fun getSaveTokenUseCase(): SaveTokenUseCase
}

class PushNotificationServices : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        val entryPoint =
            EntryPointAccessors.fromApplication(
                applicationContext,
                PushNotificationEntryPoint::class.java,
            )
        val saveTokenUseCase = entryPoint.getSaveTokenUseCase()

        (applicationContext as AppApplication).applicationScope.launch {
            saveTokenUseCase(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        message.notification?.let {
            sendNotification(it.title ?: "", it.body ?: "")
        }
    }

    private fun sendNotification(
        title: String,
        body: String,
    ) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannelIfNeeded(notificationManager)
        }

        val notification =
            NotificationCompat
                .Builder(this, CHANNEL_ID)
                .apply {
                    setSmallIcon(R.drawable.ic_favorite_pressed)
                    setContentTitle(title)
                    setContentText(body)
                    setAutoCancel(true)
                    setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    setStyle(NotificationCompat.BigTextStyle().bigText(body))
                }.build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannelIfNeeded(notificationManager: NotificationManager) {
        val channel =
            NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT,
            )
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "BulletinBoardChannel"
        private const val CHANNEL_NAME = "New Ads"
        private const val NOTIFICATION_ID = 0
    }
}
