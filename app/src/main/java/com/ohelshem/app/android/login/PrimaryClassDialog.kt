package com.ohelshem.app.android.login

import android.app.Activity
import com.afollestad.materialdialogs.GravityEnum
import com.afollestad.materialdialogs.MaterialDialog
import com.ohelshem.app.android.stringArrayRes
import com.ohelshem.app.controller.storage.TeacherStorage
import com.yoavst.changesystemohelshem.R


object PrimaryClassDialog {
    fun create(storage: TeacherStorage, activity: Activity, listener: () -> Unit): MaterialDialog {
        var classes = storage.classes
        classes = classes.sortedWith(compareBy({ it.layer }, { it.clazz }))

        val layers = activity.stringArrayRes(R.array.layers)

        return MaterialDialog.Builder(activity)
                .title(R.string.set_primary_class)
                .autoDismiss(true)
                .canceledOnTouchOutside(false)
                .cancelable(false)
                .itemsGravity(GravityEnum.CENTER)
                .items(listOf(activity.getString(R.string.no_primary_class)) + classes.map { "${layers[it.layer - 9]}'${it.clazz}" })
                .itemsCallback { _, _, which, _ ->
                    if (which == 0)
                        storage.primaryClass = null
                    else
                        storage.primaryClass = classes[which - 1]

                    listener()
                }
                .show()
    }
}