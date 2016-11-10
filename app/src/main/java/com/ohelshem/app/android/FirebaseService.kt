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
import com.ohelshem.api.model.Change
import com.ohelshem.api.model.UpdateError
import com.ohelshem.app.android.main.MainActivity
import com.ohelshem.app.controller.api.ApiController
import com.ohelshem.app.controller.api.ApiController.UpdatedApi
import com.ohelshem.app.controller.storage.Storage
import com.ohelshem.app.toCalendar
import com.yoavst.changesystemohelshem.R
import org.jetbrains.anko.notificationManager
import java.util.*

class FirebaseService : FirebaseMessagingService(), LazyKodeinAware, ApiController.Callback {
    override val kodein: LazyKodein = LazyKodein(appKodein)
    private val apiController: ApiController by instance()
    private val storage: Storage by instance()

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data?.isNotEmpty() ?: false) {
            val data = remoteMessage.data!!
            if (Notification_TitleField in data) {
                val title = data[Notification_TitleField] ?: ""
                val body = data[Notification_BodyField] ?: ""
                val callback = App.messageCallback
                if (App.isForeground && callback != null)
                    callback(title, body)
                else
                    sendNotification(title, body, showDialog = true)
            } else {
                apiController[CallbackId] = this
                initCurrentChanges()
                if (!apiController.isBusy) {
                    isFirstTimeFailure = true
                    apiController.update()
                }
            }
        }
    }

    private fun initCurrentChanges() {
        if (currentChanges == null) {
            val date = storage.changesDate
            if (changesDate != date) {
                val clazz = storage.userData.clazz
                currentChanges = storage.changes?.filter { it.clazz == clazz }
            }
        }
    }

    override fun onSuccess(apis: Set<UpdatedApi>) {
        removeCallback()
        if (UpdatedApi.Changes in apis) {
            val clazz = storage.userData.clazz
            val newChanges = storage.changes?.filter { it.clazz == clazz }

            val current = currentChanges

            val areNoChangesNew = newChanges == null && current == null && storage.changesDate != changesDate &&
                    storage.changesDate.toCalendar()[Calendar.DAY_OF_YEAR] != changesDate.toCalendar()[Calendar.DAY_OF_YEAR]
            val isThereDiff = newChanges == null && current != null ||
                    newChanges != null && current == null ||
                    newChanges?.size != current?.size ||
                    newChanges?.any { new -> current?.none { it.color == new.color && it.content == new.content } ?: false } ?: false

            if (areNoChangesNew) {
                notifyNoChanges()
            } else if (isThereDiff) {
                if (current?.size ?: 0 == 0) {
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
        sendNotification(getString(R.string.changes_notif_title), getString(R.string.no_changes))
    }

    private fun notifyChanges() {
        sendNotification(getString(R.string.changes_notif_title), getString(R.string.enter_to_see_changes))
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
                UpdateError.Connection -> sendNotification(getString(R.string.no_connection), getString(R.string.no_connection_subtitle_notification))
                UpdateError.Login -> sendNotification(getString(R.string.login_error), getString(R.string.login_error_subtitle))
                UpdateError.NoData -> Unit
                UpdateError.Exception -> sendNotification(getString(R.string.general_error), getString(R.string.try_again_notification))
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
    private fun sendNotification(title: String, messageBody: String, showDialog: Boolean = false) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        if (showDialog) {
            intent.action = MainActivity.Action_Notification
            intent.putExtra(Intent.EXTRA_TITLE, title)
            intent.putExtra(Intent.EXTRA_TEXT, messageBody)
        } else {
            intent.action = MainActivity.Shortcut_LaunchChanges
        }
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
        private const val CallbackId = 82

        private var isFirstTimeFailure = true
        private var currentChanges: List<Change>? = null
        private var changesDate: Long = 0

    }
}