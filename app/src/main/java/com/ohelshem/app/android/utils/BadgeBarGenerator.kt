package com.ohelshem.app.android.utils

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.ohelshem.api.model.ClassInfo
import com.ohelshem.app.android.hide
import com.ohelshem.app.android.setMargins
import com.ohelshem.app.android.show
import com.ohelshem.app.android.stringArrayRes
import com.yoavst.changesystemohelshem.R
import kotlinx.android.synthetic.main.teacher_badge_layout.view.*
import org.jetbrains.anko.*

object BadgeBarGenerator {
    fun inflate(view: ViewGroup, classes: List<ClassInfo>, firstStaticBadgeText: String? = null, secondStaticBadgeText: String? = null,
                firstTag: ClassInfo? = null, secondTag: ClassInfo? = null, background: Drawable? = null, moreListener: (() -> Unit)? = null, listener: (ClassInfo?) -> Unit) {
        val context = view.context
        val layers = context.stringArrayRes(R.array.layers)
        val spacing = (context.resources.displayMetrics.density * 4).toInt()
        val badges = mutableListOf<View>(view.staticBadge, view.secondStaticBadge)

        val firstBadge = view.staticBadge
        firstBadge.tag = firstTag
        if (firstStaticBadgeText != null) {
            firstBadge.show()
            firstBadge.text = firstStaticBadgeText
            firstBadge.isSelected = true
        } else
            firstBadge.hide()

        val secondBadge = view.secondStaticBadge
        secondBadge.tag = secondTag
        if (secondStaticBadgeText != null) {
            secondBadge.show()
            secondBadge.text = secondStaticBadgeText

            firstBadge.isSelected = false
            secondBadge.isSelected = true
        } else
            secondBadge.hide()

        val size = classes.size
        val badgesLayout = view.badgesLayout
        classes.forEachIndexed { i, clazz ->
            val badge = context.generateBadge("${layers[clazz.layer - 9]}'${clazz.clazz}", background)
            badge.tag = clazz
            badge.setMargins(left = if (i == 0) 0 else spacing, right = if (i == size) 0 else spacing)
            badgesLayout.addView(badge)
            badges += badge
        }

        val badgesLayoutScroll = view.badgesLayoutScroll
        badgesLayoutScroll.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(view: View, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
                badgesLayoutScroll.removeOnLayoutChangeListener(this)
                badgesLayoutScroll.fullScroll(View.FOCUS_RIGHT)
            }
        })

        view.tag = badges
        badges.forEach { badge ->
            badge.onClick {
                if (!badge.isSelected) {
                    badges.forEach { it.isSelected = false }
                    badge.isSelected = true
                    listener(badge.tag as? ClassInfo)
                }
            }
        }

        if (moreListener != null) {
            view.more.onClick {
                moreListener()
            }
        } else view.more.hide()
    }

    fun badgesDisableAll(view: ViewGroup) {
        @Suppress("UNCHECKED_CAST")
        (view.tag as List<View>).forEach { it.isSelected = false }
    }

    private fun Context.generateBadge(text: String, background: Drawable?): View {
        val view = TextView(this)
        if (background != null)
            view.background = background
        else
            view.backgroundResource = R.drawable.badge_background
        view.text = text
        view.textColor = Color.WHITE
        view.layoutParams = LinearLayout.LayoutParams(wrapContent, dip(40))
        return view
    }
}