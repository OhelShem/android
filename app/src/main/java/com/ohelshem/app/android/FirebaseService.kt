package com.ohelshem.app.android

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ohelshem.app.android.notifications.ChangesNotificationGenerator
import com.ohelshem.app.android.notifications.sendNotification
import org.jetbrains.anko.notificationManager

class FirebaseService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data?.isNotEmpty() ?: false)
            parseData(remoteMessage.data)
    }

    private fun parseData(data: Map<String, String>) {
        if (Notification_MsgMode in data) {
            val title = data["msg_title"] ?: "הודעה מאהל שם"
            val body = data["msg_body"] ?: ""

            if (body == "---")
                notificationManager.cancel(RemoteNotificationId)
            else
                sendNotification(title, body, RemoteNotificationId, null, big = true, sound = true)

        } else
            ChangesNotificationGenerator(this).prepareNotification()
    }

    companion object {
        private const val Notification_MsgMode = "msg_mode"
        private const val RemoteNotificationId = 73
    }
}