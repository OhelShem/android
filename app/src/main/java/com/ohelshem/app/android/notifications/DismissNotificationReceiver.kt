package com.ohelshem.app.android.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.github.salomonbrys.kodein.LazyKodein
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.erased.instance
import com.ohelshem.app.clearTime
import com.ohelshem.app.controller.storage.Storage
import com.ohelshem.app.getIsraelCalendar
import org.jetbrains.anko.notificationManager


class DismissNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val kodein = LazyKodein(context.appKodein)
        val storage = kodein.instance<Storage>().value
        storage.ongoingNotificationDisableDate = getIsraelCalendar().clearTime().timeInMillis
        context.notificationManager.cancel(NotificationId)
    }

    companion object {
        private const val NotificationId = 1342
    }

}