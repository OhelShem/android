package com.ohelshem.app.android.login

import android.app.Activity
import android.content.Context
import android.support.v4.content.res.ResourcesCompat
import android.view.View
import au.com.dardle.widget.BadgeLayout
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog
import com.github.javiersantos.materialstyleddialogs.enums.Style
import com.ohelshem.api.model.ClassInfo
import com.ohelshem.app.android.stringArrayRes
import com.ohelshem.app.controller.storage.TeacherStorage
import com.yoavst.changesystemohelshem.R
import kotlinx.android.synthetic.main.view_badge_layout.view.*
import org.jetbrains.anko.dip
import org.jetbrains.anko.layoutInflater

object PrimaryClassDialog {
    fun create(storage: TeacherStorage, activity: Activity, listener: () -> Unit): MaterialStyledDialog {
        val classes = storage.classes
        val padding = activity.dip(8)
        var selectedClass: ClassInfo? = classes.first()

        return MaterialStyledDialog.Builder(activity)
                .setStyle(Style.HEADER_WITH_TITLE)
                .setTitle(R.string.set_primary_class)
                .setCustomView(createView(classes, activity, showNoClasses = true) { selectedClass = it }, padding, padding, padding, padding)
                .setCancelable(false)
                .setPositiveText(R.string.save)
                .onPositive { materialDialog, dialogAction ->
                    storage.primaryClass = selectedClass
                    listener()
                }
                .show()
    }

    fun createView(classes: List<ClassInfo>, context: Context, showNoClasses: Boolean, listener: (ClassInfo?) -> Unit): View {
        val view = context.layoutInflater.inflate(R.layout.view_badge_layout, null, false)
        val badgeLayout = view.badgeLayout
        val layers = context.stringArrayRes(R.array.layers)
        val badges = mutableListOf<BadgeLayout.Badge>()

        badgeLayout.addOnBadgeClickedListener {
            badges.forEach { it.setSelected(false) }
            it.setSelected(true)

            val parts = it.text!!.split('\'')
            if (parts.size == 1) {
                listener(null)
            } else {
               listener(ClassInfo(layers.indexOf(parts[0]) + 9, parts[1].toInt()))
            }

        }

        badgeLayout.setBadgeBackground(R.drawable.badge_background)
        badgeLayout.setSpacing((context.resources.displayMetrics.density * 8).toInt())
        badgeLayout.setBadgeTextColor(ResourcesCompat.getColorStateList(context.resources, android.R.color.white, context.theme))

        classes.forEach {
            badgeLayout.addBadge(badgeLayout.newBadge().setText("${layers[it.layer - 9]}'${it.clazz}").apply { badges += this })
        }
        if (showNoClasses) {
            badgeLayout.addBadge(badgeLayout.newBadge().setText(context.getString(R.string.no_primary_class)).setEnabled(true).apply { badges += this })
        }
        return view
    }
}