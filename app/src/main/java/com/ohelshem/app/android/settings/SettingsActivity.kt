/*
 * Copyright 2010-2015 Yoav Sternberg.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.ohelshem.app.android.settings

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.graphics.ColorUtils
import android.support.v7.app.AppCompatDelegate
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.LinearLayout
import com.github.salomonbrys.kodein.erased.instance
import com.jakewharton.processphoenix.ProcessPhoenix
import com.ohelshem.api.model.ClassInfo
import com.ohelshem.app.android.*
import com.ohelshem.app.android.notifications.OngoingNotificationService
import com.ohelshem.app.android.utils.AppThemedActivity
import com.ohelshem.app.controller.analytics.Analytics
import com.ohelshem.app.controller.storage.SharedStorage.Theme
import com.yoavst.changesystemohelshem.BuildConfig.*
import com.yoavst.changesystemohelshem.R
import kotlinx.android.synthetic.main.settings_activity.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk15.listeners.onCheckedChange
import org.jetbrains.anko.sdk15.listeners.onClick
import org.jetbrains.anko.sdk15.listeners.onItemSelectedListener


class SettingsActivity : AppThemedActivity() {
    private val analyticsManager: Analytics by kodein.instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        initLayout()
        initViews()
    }

    private fun initLayout() {
        restartApp.setColorFilter(themesHeader.currentTextColor)

        // settings card for teachers
        if (storage.userData.isTeacher()) {
            teacherSettings.apply {
                val classes = storage.classes.sortedWith(compareBy({ it.layer }, { it.clazz }))
                val options = mutableListOf(getString(R.string.no_primary_class_short))
                val flags = mutableListOf(ClassInfo(0, 0))

                val layers = stringArrayRes(R.array.layers)
                classes.forEach {
                    options += "${layers[it.layer - 9]}'${it.clazz}"
                    flags += it
                }

                settingsItem(getString(R.string.primary_class_setting), items = options, default = flags.indexOf(storage.primaryClass)) {
                    if (it == 0) {
                        storage.primaryClass = null
                        storage.notificationsForBirthdays = false
                        storage.notificationsForTests = false
                    } else {
                        storage.primaryClass = flags[it]
                    }
                }
            }
        } else {
            teacherSettingsCard.hide()
        }

        notificationsList.apply {
            if (storage.isStudent()) {
                settingsItem(getString(R.string.changes_realtime), showCheckBox = true, isChecked = storage.notificationsForChanges) {
                    storage.notificationsForChanges = it
                    if (it)
                        analyticsManager.subscribe()
                    else
                        analyticsManager.unsubscribe()
                }
            }
            settingsItem(getString(R.string.timetable_notif), showCheckBox = true, isChecked = storage.notificationsForTimetable) {
                storage.notificationsForTimetable = it
                storage.ongoingNotificationDisableDate = 0
                OngoingNotificationService.update(this@SettingsActivity)
            }
            settingsItem(getString(if (storage.isStudent()) R.string.birthdays_in_class else R.string.birthdays_in_class_teacher), showCheckBox = true, isChecked = storage.notificationsForBirthdays) {
                storage.notificationsForBirthdays = it
            }
            if (storage.isStudent()) {
                settingsItem(getString(R.string.tests), showCheckBox = true, isChecked = storage.notificationsForTests) {
                    storage.notificationsForTests = it
                }
            }
            settingsItem(getString(R.string.holidays_notif), showCheckBox = true, isChecked = storage.notificationsForHolidays) {
                storage.notificationsForHolidays = it
            }
        }


        themeList.apply {
            val options = stringArrayRes(R.array.night_mode_options)
            val flags = intArrayOf(AppCompatDelegate.MODE_NIGHT_NO, AppCompatDelegate.MODE_NIGHT_YES, AppCompatDelegate.MODE_NIGHT_AUTO)
            settingsItem(getString(R.string.dark_mode), items = options.toList(), default = flags.indexOf(storage.darkMode)) {
                storage.darkMode = flags[it]
            }
        }
        val currentMode = storage.theme
        val rippleColor = rippleColor

        initColor(redTheme, colorRes(R.color.red_light), rippleColor, colorRes(R.color.red), currentMode == Theme.Red)
        redTheme.onClick {
            storage.theme = Theme.Red
            selectTheme(red = true)
        }
        initColor(blueTheme, colorRes(R.color.blue_light), rippleColor, colorRes(R.color.blue), currentMode == Theme.Blue)
        blueTheme.onClick {
            storage.theme = Theme.Blue
            selectTheme(blue = true)
        }
        initColor(greenTheme, colorRes(R.color.green_light), rippleColor, colorRes(R.color.green), currentMode == Theme.Green)
        greenTheme.onClick {
            storage.theme = Theme.Green
            selectTheme(green = true)
        }

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

    private fun selectTheme(red: Boolean = false, green: Boolean = false, blue: Boolean = false) {
        redTheme.isSelected = red
        greenTheme.isSelected = green
        blueTheme.isSelected = blue

    }

    private fun initViews() {
        toolbar.title = ""
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val data = storage.userData
        if (storage.isStudent())
            profile.htmlText = resources.getString(R.string.logged_in_as_student, data.privateName, data.familyName, stringArrayRes(R.array.layers)[data.layer - 9], data.clazz)
        else
            profile.htmlText = resources.getString(R.string.logged_in_as_teacher, data.privateName, data.familyName)

        exitButton.onClick {
            setResult(42)
            finish()
        }
        openSourceButton.onClick {
            browse("https://ohelshem.github.io/android")
        }
        facebookButton.onClick {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getFacebookPageURL())))
        }
        siteButton.onClick {
            browse("http://www.ohel-shem.com/portal6")
        }
        overridesButton.onClick {
            startActivity<OverridesActivity>()
        }
        restartApp.onClick {
            ProcessPhoenix.triggerRebirth(this)
        }

        versionName.htmlText = "${versionName.text}: <b>$VERSION_NAME</b>"
        versionCode.htmlText = "${versionCode.text}: <b>$VERSION_CODE</b>"
        versionType.htmlText = "${versionType.text}: <b>$BUILD_TYPE</b>"
        versionCode.onClick {
            if (!storage.developerMode) {
                numberOfTaps++
                handler.removeCallbacksAndMessages(null)
                if (numberOfTaps == 7) {
                    storage.developerMode = true
                    toast("Developer mode enabled")
                } else handler.postDelayed(request, 500)
            }
        }

    }

    private var numberOfTaps: Int = 0
    private val handler = Handler()
    private val request: Runnable = Runnable {
        numberOfTaps = 0
    }

    fun LinearLayout.settingsItem(title: String, subtitle: String = "", showCheckBox: Boolean = false, isChecked: Boolean = false, listener: ((checked: Boolean) -> Unit)): LinearLayout {
        return linearLayout {
            orientation = LinearLayout.HORIZONTAL
            bottomPadding = dip(5)
            topPadding = dip(5)
            backgroundResource = resourceAttr(R.attr.selectableItemBackground)
            if (showCheckBox) {
                appCompatSwitch {
                    setChecked(isChecked)
                    onCheckedChange { _, b -> listener(b) }
                }.lparams(width = wrapContent, height = matchParent)
                onClick { (getChildAt(0) as CompoundButton).toggle() }
            } else onClick { listener(false) }
            linearLayout {
                orientation = LinearLayout.VERTICAL

                textView {
                    text = title
                    gravity = Gravity.RIGHT
                    textSize = 18f
                }.lparams(width = matchParent, height = wrapContent)
                if (subtitle.isNotEmpty())
                    textView {
                        text = subtitle
                        gravity = Gravity.RIGHT
                        textSize = 14f
                    }.lparams(width = matchParent, height = wrapContent)
            }.lparams(width = matchParent, height = matchParent)
        }
    }

    fun LinearLayout.settingsItem(title: String, subtitle: String = "", items: List<String>, default: Int, listener: ((selected: Int) -> Unit)): LinearLayout {
        return linearLayout {
            orientation = LinearLayout.HORIZONTAL
            bottomPadding = dip(5)
            topPadding = dip(5)
            backgroundResource = resourceAttr(R.attr.selectableItemBackground)
            spinner {
                val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, items)
                adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                setAdapter(adapter)
                setSelection(default)
                onItemSelectedListener {
                    onItemSelected { _, _, position, _ ->
                        listener(position)
                    }
                }
            }.lparams(width = wrapContent, height = matchParent)
            linearLayout {
                orientation = LinearLayout.VERTICAL

                textView {
                    text = title
                    gravity = Gravity.RIGHT
                    textSize = 18f
                }.lparams(width = matchParent, height = wrapContent)
                if (subtitle.isNotEmpty())
                    textView {
                        text = subtitle
                        gravity = Gravity.RIGHT
                        textSize = 14f
                    }.lparams(width = matchParent, height = wrapContent)
            }.lparams(width = matchParent, height = matchParent)
        }
    }

    companion object {
        const val FACEBOOK_PAGE_ID = "ohelshem.school"
        const val FACEBOOK_URL = "https://www.facebook.com/$FACEBOOK_PAGE_ID"

        private fun Context.getFacebookPageURL(): String {
            val packageManager = packageManager
            try {
                val versionCode = packageManager.getPackageInfo("com.facebook.katana", 0).versionCode
                if (versionCode >= 3002850) {
                    return "fb://facewebmodal/f?href=" + FACEBOOK_URL
                } else {
                    return "fb://page/" + FACEBOOK_PAGE_ID
                }
            } catch (e: PackageManager.NameNotFoundException) {
                return FACEBOOK_URL
            }
        }
    }
}