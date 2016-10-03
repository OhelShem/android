package com.ohelshem.app.android

import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.support.v7.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.yoavst.changesystemohelshem.R
import org.jetbrains.anko.notificationManager

class FirebaseService: FirebaseMessagingService() {
    val TAG = "FirebaseService"
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        //FIXME
        if (remoteMessage.notification == null) {

        }
    }

    /**
     * Create and show a simple notification containing the received FCM message.

     * @param messageBody FCM message body received.
     */
    private fun sendNotification(messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.ohel_shem_notification_title))
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

        notificationManager.notify(42, notificationBuilder.build())
    }
}