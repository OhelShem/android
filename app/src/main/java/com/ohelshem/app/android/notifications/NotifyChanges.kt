package com.ohelshem.app.android.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.support.v7.app.NotificationCompat
import com.github.salomonbrys.kodein.LazyKodein
import com.github.salomonbrys.kodein.LazyKodeinAware
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.erased.instance
import com.ohelshem.api.model.Change
import com.ohelshem.api.model.UpdateError
import com.ohelshem.app.android.App
import com.ohelshem.app.android.fromHtml
import com.ohelshem.app.android.main.MainActivity
import com.ohelshem.app.controller.api.ApiController
import com.ohelshem.app.controller.storage.Storage
import com.ohelshem.app.toCalendar
import com.yoavst.changesystemohelshem.R
import org.jetbrains.anko.notificationManager
import java.util.*

class NotifyChanges : LazyKodeinAware, ApiController.Callback {

    override val kodein: LazyKodein = LazyKodein(App.instance.appKodein)
    private val
            apiController: ApiController by instance()
    private val storage: Storage by instance()
    
    private var myContext: Context? = null

    fun notifyC(context: Context) {
        myContext = context
        if (storage.notificationsForChanges)
            queryApiForNotification()
    }

    fun queryApiForNotification() {
        apiController[CallbackId] = this
        initCurrentChanges()
        if (!apiController.isBusy) {
            isFirstTimeFailure = true
            apiController.update()
        }
    }

    private fun initCurrentChanges() {
        if (currentChanges == null) {
            val date = storage.changesDate
            if (changesDate != date) {
                val clazz = storage.userData.clazz
                currentChanges = storage.changes?.filter { it.clazz == clazz }
                changesDate = date
            }
        }
    }

    override fun onSuccess(apis: Set<ApiController.UpdatedApi>) {
        removeCallback()
        if (ApiController.UpdatedApi.Changes in apis) {
            val clazz = storage.userData.clazz
            val newChanges = storage.changes?.filter { it.clazz == clazz }

            val current = currentChanges
            val areNoChangesNew = newChanges == null && current == null && storage.changesDate != changesDate &&
                    storage.changesDate.toCalendar()[Calendar.DAY_OF_YEAR] != changesDate.toCalendar()[Calendar.DAY_OF_YEAR]
            val isThereDiff = newChanges?.size ?: 0 != current?.size ?: 0 ||
                    newChanges?.any { new -> current?.none { it.color == new.color && it.content.trim() == new.content.trim() } ?: false } ?: false

            if (areNoChangesNew) {
                notifyNoChanges()
            } else if (isThereDiff) {
                if (newChanges?.size ?: 0 == 0) {
                    notifyNoChanges()
                } else {
                    notifyChanges()
                }
            } else {
                // No Diff in change, do nothing.
            }

            currentChanges = null
            changesDate = 0
        }
    }

    private fun notifyNoChanges() {
        sendNotification(myContext!!.getString(R.string.changes_notif_title), myContext!!.getString(R.string.no_changes))
    }

    private fun notifyChanges() {
        sendNotification(myContext!!.getString(R.string.changes_notif_title), myContext!!.getString(R.string.enter_to_see_changes))
    }

    override fun onFail(error: UpdateError) {
        if (isFirstTimeFailure) {
            isFirstTimeFailure = false
            apiController.update()
        } else {
            removeCallback()
            currentChanges = null
            changesDate = 0
            when (error) {
                UpdateError.Connection -> sendNotification(myContext!!.getString(R.string.no_connection), myContext!!.getString(R.string.no_connection_subtitle_notification))
                UpdateError.Login -> sendNotification(myContext!!.getString(R.string.login_error), myContext!!.getString(R.string.login_error_subtitle))
                UpdateError.NoData -> Unit
                UpdateError.Exception -> sendNotification(myContext!!.getString(R.string.general_error), myContext!!.getString(R.string.try_again_notification))
            }
        }
    }

    private fun removeCallback() {
        apiController -= CallbackId
    }

    /**
     * Create and show a simple notification containing the received FCM message.

     * @param messageBody FCM message body received.
     */
    fun sendNotification(title: String, messageBody: String, showDialog: Boolean = false) {
        val intent = Intent(myContext!!, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        if (showDialog) {
            intent.action = MainActivity.Action_Notification
            intent.putExtra(Intent.EXTRA_TITLE, title)
            intent.putExtra(Intent.EXTRA_TEXT, messageBody)
        } else {
            intent.action = MainActivity.Shortcut_LaunchChanges
        }
        val pendingIntent = PendingIntent.getActivity(myContext!!, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(myContext!!)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(messageBody.fromHtml())
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

        myContext!!.notificationManager.notify(42, notificationBuilder.build())
    }

    companion object {
        private const val CallbackId = 82

        private var isFirstTimeFailure = true
        private var currentChanges: List<Change>? = null
        private var changesDate: Long = 0

    }




}