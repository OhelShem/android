package com.ohelshem.app.android.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ohelshem.app.android.App

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, callingIntent: Intent?) {
        App.setAlarm(context)
    }
}