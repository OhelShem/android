package com.ohelshem.app.android

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ohelshem.app.android.notifications.NotifyChanges

class FirebaseService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data?.isNotEmpty() ?: false) {
            val data = remoteMessage.data!!
            parseData(data)
        }
    }

    private fun parseData(data: Map<String, String>) {
        if (Notification_TitleField in data) {
            val title = data[Notification_TitleField] ?: ""
            val body = data[Notification_BodyField] ?: ""
            val callback = App.messageCallback
            if (App.isForeground && callback != null)
                callback(title, body)
            else
                NotifyChanges().sendNotification(title, body, showDialog = true)
        } else {
            NotifyChanges().notifyC(this)
        }
    }

    companion object {
        private const val Notification_TitleField = "title"
        private const val Notification_BodyField = "body"

    }
}