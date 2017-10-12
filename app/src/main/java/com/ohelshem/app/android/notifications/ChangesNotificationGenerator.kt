package com.ohelshem.app.android.notifications

import android.content.Context
import com.github.salomonbrys.kodein.LazyKodein
import com.github.salomonbrys.kodein.LazyKodeinAware
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.erased.instance
import com.ohelshem.api.model.Change
import com.ohelshem.api.model.UpdateError
import com.ohelshem.app.controller.api.ApiController
import com.ohelshem.app.controller.api.ApiController.UpdatedApi
import com.ohelshem.app.controller.storage.Storage
import com.ohelshem.app.toCalendar
import com.yoavst.changesystemohelshem.R
import java.util.*

class ChangesNotificationGenerator(context: Context) : LazyKodeinAware, ApiController.Callback {
    override val kodein: LazyKodein = LazyKodein(context.appKodein)
    private val context: Context = context.applicationContext

    private val apiController: ApiController by instance()
    private val storage: Storage by instance()

    fun prepareNotification() {
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

    override fun onSuccess(apis: Set<UpdatedApi>) {
        removeCallback()
        if (UpdatedApi.Changes in apis) {
            val clazz = storage.userData.clazz
            val newChanges = storage.changes?.filter { it.clazz == clazz }

            val current = currentChanges
            val areNoChangesNew = newChanges == null && current == null && storage.changesDate != changesDate &&
                    storage.changesDate.toCalendar()[Calendar.DAY_OF_YEAR] != changesDate.toCalendar()[Calendar.DAY_OF_YEAR]
            val isThereDiff = newChanges?.size ?: 0 != current?.size ?: 0 ||
                    newChanges?.any { new -> current?.none { it.hour == new.hour && it.color == new.color && it.content.trim() == new.content.trim() } == true } == true

            changesForTomorrow = (storage.changesDate.toCalendar()[Calendar.DAY_OF_YEAR] != changesDate.toCalendar()[Calendar.DAY_OF_YEAR])

            if (storage.changes?.isNotEmpty() == true || isThereDiff) {
                if (areNoChangesNew) {
                    notifyNoChanges()
                } else if (isThereDiff) {
                    if (newChanges?.size ?: 0 == 0) {
                        notifyNoChanges()
                    } else {
                        notifyChanges()
                    }
                }
            }
            currentChanges = null
            changesDate = 0
        }
    }

    private fun notifyNoChanges() = context.sendNotification(
            if (changesForTomorrow) context.getString(R.string.changes_notif_title_tmrw) else context.getString(R.string.changes_notif_title),
            context.getString(R.string.no_changes), NotificationId, sound = true
    )

    private fun notifyChanges() {
        context.sendNotification(
                if (changesForTomorrow)
                    context.getString(R.string.changes_notif_title_tmrw)
                else
                    context.getString(R.string.changes_notif_title), context.getString(R.string.enter_to_see_changes),
                NotificationId, sound = true)
    }

    override fun onFail(error: UpdateError) {
        if (isFirstTimeFailure) {
            isFirstTimeFailure = false
            apiController.update()
        } else {
            removeCallback()
            currentChanges = null
            changesDate = 0
        }
    }

    private fun removeCallback() {
        apiController -= CallbackId
    }


    companion object {
        private const val CallbackId = 82
        private const val NotificationId = 55

        private var isFirstTimeFailure = true
        private var currentChanges: List<Change>? = null
        private var changesDate: Long = 0
        private var changesForTomorrow = false

    }


}