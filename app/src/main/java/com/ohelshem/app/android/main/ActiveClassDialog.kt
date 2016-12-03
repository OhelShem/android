package com.ohelshem.app.android.login

import android.app.Activity
import com.afollestad.materialdialogs.GravityEnum
import com.afollestad.materialdialogs.MaterialDialog
import com.ohelshem.api.model.ClassInfo
import com.ohelshem.app.android.stringArrayRes
import com.yoavst.changesystemohelshem.R


object ActiveClassDialog {
    fun create(activity: Activity, schoolClasses: List<ClassInfo>): MaterialDialog.Builder {
        val layers = activity.stringArrayRes(R.array.layers)
        return MaterialDialog.Builder(activity)
                .title(R.string.set_current_class)
                .autoDismiss(true)
                .canceledOnTouchOutside(true)
                .cancelable(true)
                .itemsGravity(GravityEnum.CENTER)
                .items(schoolClasses.map { "${layers[it.layer - 9]}'${it.clazz}" })
                .positiveText(R.string.accept)
    }
}