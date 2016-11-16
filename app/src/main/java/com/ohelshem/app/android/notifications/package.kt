package com.ohelshem.app.android.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.support.v7.app.NotificationCompat
import com.ohelshem.app.android.fromHtml
import com.ohelshem.app.android.main.MainActivity
import com.yoavst.changesystemohelshem.R
import org.jetbrains.anko.notificationManager

/**
 * Create and show a simple notification
 */
fun Context.sendNotification(title: String, messageBody: String, id: Int, showDialog: Boolean = false) {
    val intent = Intent(this, MainActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    if (showDialog) {
        intent.action = MainActivity.Action_Notification
        intent.putExtra(Intent.EXTRA_TITLE, title)
        intent.putExtra(Intent.EXTRA_TEXT, messageBody)
    } else {
        intent.action = MainActivity.Shortcut_LaunchChanges
    }
    val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

    notificationManager.notify(id, NotificationCompat.Builder(this)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(messageBody.fromHtml())
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentIntent(pendingIntent).build())
}

fun Context.sendNotification(title: String, messageBody: String, id: Int, action: String, big: Boolean = false) {
    val intent = Intent(this, MainActivity::class.java).setAction(action)
    val pIntent = PendingIntent.getActivity(this, 0, intent, 0)
    if (big) {
        notificationManager.notify(id, NotificationCompat.Builder(this)
                .apply { if (big) setStyle(android.support.v4.app.NotificationCompat.BigTextStyle().bigText(messageBody)) }
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setContentIntent(pIntent).build())
    }
}


