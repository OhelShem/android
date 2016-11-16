package com.ohelshem.app.android

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ohelshem.app.android.notifications.ChangesNotificationGenerator
import com.ohelshem.app.android.notifications.sendNotification

class FirebaseService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data?.isNotEmpty() ?: false)
            parseData(remoteMessage.data)
    }

    private fun parseData(data: Map<String, String>) {
        if (Notification_TitleField in data) {
            val title = data[Notification_TitleField] ?: ""
            val body = data[Notification_BodyField] ?: ""
            val callback = App.messageCallback
            if (App.isForeground && callback != null)
                callback(title, body)
            else
                sendNotification(title, body, RemoteNotificationId, showDialog = true)
        } else
            ChangesNotificationGenerator(this).prepareNotification()
    }

    companion object {
        private const val Notification_TitleField = "title"
        private const val Notification_BodyField = "body"
        private const val RemoteNotificationId = 73
    }
}