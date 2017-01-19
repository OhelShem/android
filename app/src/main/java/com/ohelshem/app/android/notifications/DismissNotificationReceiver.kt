package com.ohelshem.app.android.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.github.salomonbrys.kodein.LazyKodein
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.erased.instance
import com.ohelshem.app.controller.storage.Storage
import org.jetbrains.anko.notificationManager
import java.util.*


class DismissNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val kodein = LazyKodein(context.appKodein)
        val storage = kodein.instance<Storage>()
        storage.value.ongoingNotificationDisableDate = toDayOnly(Calendar.getInstance()).timeInMillis
        context.notificationManager.cancel(NotificationId)
    }

    companion object {
        private const val NotificationId = 1342
    }

    fun toDayOnly(cal: Calendar): Calendar {
        cal.set(Calendar.HOUR,0)
        cal.set(Calendar.MINUTE,0)
        cal.set(Calendar.SECOND,0)
        cal.set(Calendar.MILLISECOND,0)
        return cal
    }

}