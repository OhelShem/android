package com.ohelshem.app.android.login

import android.app.Activity
import android.os.Build
import android.support.v4.graphics.ColorUtils
import android.support.v7.app.AppCompatDelegate.*
import android.view.View
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog
import com.github.javiersantos.materialstyleddialogs.enums.Style
import com.ohelshem.app.android.colorRes
import com.ohelshem.app.android.getPressedColorRippleDrawable
import com.ohelshem.app.android.getStateDrawable
import com.ohelshem.app.android.utils.AttributeExtractor
import com.ohelshem.app.controller.storage.SharedStorage
import com.yoavst.changesystemohelshem.R
import kotlinx.android.synthetic.main.theme_dialog.view.*
import org.jetbrains.anko.backgroundDrawable
import org.jetbrains.anko.dip
import org.jetbrains.anko.onClick

object WelcomeDialog {
    fun create(storage: SharedStorage, activity: Activity, listener: () -> Unit): MaterialStyledDialog {
        val view = activity.layoutInflater.inflate(R.layout.theme_dialog, null, false)
        view.nightMode.check(R.id.auto)

        val rippleColor = AttributeExtractor.extractRippleColorFrom(activity)

        initColor(view.redTheme, activity.colorRes(R.color.red_light), rippleColor, activity.colorRes(R.color.red),false)
        view.redTheme.onClick {
            selectTheme(view, red = true)
        }
        initColor(view.blueTheme, activity.colorRes(R.color.blue_light), rippleColor, activity.colorRes(R.color.blue), true)
        view.blueTheme.onClick {
            selectTheme(view, blue = true)
        }
        initColor(view.greenTheme, activity.colorRes(R.color.green_light), rippleColor, activity.colorRes(R.color.green), false)
        view.greenTheme.onClick {
            selectTheme(view, green = true)
        }

        val padding = activity.dip(8)

        return MaterialStyledDialog.Builder(activity)
                .setStyle(Style.HEADER_WITH_TITLE)
                .setTitle(R.string.theme_settings)
                .setCustomView(view, padding, padding, padding, padding)
                .setCancelable(false)
                .setPositiveText(R.string.save)
                .onPositive { materialDialog, dialogAction ->
                    val selection = view.nightMode.checkedRadioButtonId
                    storage.darkMode = if (selection == R.id.auto) MODE_NIGHT_AUTO else if (selection == R.id.enabled) MODE_NIGHT_YES else MODE_NIGHT_NO

                    if (view.redTheme.isSelected)
                        storage.theme = SharedStorage.Theme.Red
                    else if (view.greenTheme.isSelected)
                        storage.theme = SharedStorage.Theme.Green
                    else
                        storage.theme = SharedStorage.Theme.Blue

                    listener()

                }
                .show()

    }

    private fun initColor(view: View?, color: Int, rippleColor: Int, darkColor: Int, isEnabled: Boolean) {
        if (view != null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
                view.backgroundDrawable = getStateDrawable(color, darkColor, ColorUtils.compositeColors(rippleColor, darkColor))
            else
                view.backgroundDrawable = getPressedColorRippleDrawable(color, darkColor, darkColor)
            if (isEnabled)
                view.isSelected = true
        }
    }

    private fun selectTheme(view: View, red: Boolean = false, green: Boolean = false, blue: Boolean = false) {
        view.redTheme.isSelected = red
        view.greenTheme.isSelected = green
        view.blueTheme.isSelected = blue

    }
}