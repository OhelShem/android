package com.ohelshem.app.android

import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.support.v7.app.NotificationCompat
import com.github.salomonbrys.kodein.LazyKodein
import com.github.salomonbrys.kodein.LazyKodeinAware
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.erased.instance
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ohelshem.app.android.main.MainActivity
import com.ohelshem.app.controller.api.ApiController
import com.yoavst.changesystemohelshem.R
import org.jetbrains.anko.notificationManager

class FirebaseService : FirebaseMessagingService(), LazyKodeinAware {
    override val kodein: LazyKodein = LazyKodein(appKodein)
    private val apiController: ApiController by instance()

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data?.isNotEmpty() ?: false) {
            val data = remoteMessage.data!!
            if (Notification_TitleField in data) {
                val title = data[Notification_TitleField]
                val body = data[Notification_BodyField]
                val callback = App.messageCallback
                if (App.isForeground && callback != null)
                    callback(remoteMessage.notification.title, remoteMessage.notification.body)
                else
                    sendNotification(remoteMessage.notification.title, remoteMessage.notification.body)
            } else {
                //FIXME
            }
        }
    }

    /**
     * Create and show a simple notification containing the received FCM message.

     * @param messageBody FCM message body received.
     */
    private fun sendNotification(title: String, messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.action = MainActivity.Action_Notification
        intent.putExtra(Intent.EXTRA_TITLE, title)
        intent.putExtra(Intent.EXTRA_TEXT, messageBody)
        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(messageBody.fromHtml())
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

        notificationManager.notify(42, notificationBuilder.build())
    }

    companion object {
        private const val Notification_TitleField = "title"
        private const val Notification_BodyField = "body"

    }
}