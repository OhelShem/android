package com.ohelshem.app.android.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.erased.instance
import com.ohelshem.app.android.App
import com.ohelshem.app.controller.analytics.Analytics
import com.ohelshem.app.controller.storage.Storage

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, callingIntent: Intent?) {
        App.setAlarm(context)

        val kodein = context.appKodein()
        if (kodein.instance<Storage>().isSetup())
            kodein.instance<Analytics>().onLogin()
    }
}