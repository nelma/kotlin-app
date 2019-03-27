package com.example.kotlinapp.service

import android.util.Log
import com.example.kotlinapp.notification.NotificationCreation
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    //public static e final
    //var de classe imutavel
    companion object {
        const val TAG = "FMService"
    }

    override fun onNewToken(token: String?) {
        super.onNewToken(token)

        Log.i("TAG", token)

        FirebaseMessaging.getInstance().subscribeToTopic("MAIN")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val notification = remoteMessage.notification

        Log.i(TAG, "FCM Notification Message: $notification")
        Log.i(TAG, "FCM Message ID: ${remoteMessage.messageId}")
        Log.i(TAG, "FCM Message DATA: ${remoteMessage.data}")

        notification?.let {
            val title = it.title ?: ""
            val body = it.body ?: ""
            val data = remoteMessage.data

            Log.i(TAG, "FCM Notification title $title")
            Log.i(TAG, "FCM Notification body $body")

            NotificationCreation.create(this, title, body)
        }
    }
}
