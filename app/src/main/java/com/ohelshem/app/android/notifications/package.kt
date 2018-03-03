package com.ohelshem.app.android.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.support.v4.app.NotificationCompat
import com.ohelshem.app.android.fromHtml
import com.ohelshem.app.android.main.MainActivity
import com.yoavst.changesystemohelshem.R
import org.jetbrains.anko.notificationManager




/**
 * Create and show a simple notification
 */
fun Context.sendNotification(title: String, messageBody: String, id: Int, showDialog: Boolean = false, sound: Boolean = false) {
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

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel("OSHNotify",
                "Ohel-Shem Notifications",
                NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)
    }

    notificationManager.notify(id, NotificationCompat.Builder(this, "OSHNotify")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(messageBody.fromHtml())
            .setAutoCancel(true)
            .apply { if (sound) setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)) }
            .setContentIntent(pendingIntent).build())
}

fun Context.sendNotification(title: String, messageBody: String, id: Int, action: String?, big: Boolean = false, sound: Boolean = false, link: String = "", icon: Int = R.drawable.ic_notification) {
        val intent = Intent(this, MainActivity::class.java).setAction(action)
        val pIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val linkIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        val linkPIntent = PendingIntent.getActivity(this, 0, linkIntent, 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("OSHNotify",
                    "Ohel-Shem Notifications",
                    NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(id, NotificationCompat.Builder(this, "OSHNotify")
                .apply { if (big) setStyle(android.support.v4.app.NotificationCompat.BigTextStyle().bigText(messageBody)) }
                .setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(messageBody)
                .apply {if (sound) setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))}
                .setAutoCancel(true)
                .apply { if (action != null) setContentIntent(pIntent) else if (link.isNotEmpty()) setContentIntent(linkPIntent)}
                .build())
}


