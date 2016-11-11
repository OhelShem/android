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
import com.ohelshem.app.android.*
import com.ohelshem.app.android.notifications.OngoingNotificationService
import com.ohelshem.app.android.utils.AppThemedActivity
import com.ohelshem.app.android.utils.AttributeExtractor
import com.ohelshem.app.controller.analytics.Analytics
import com.ohelshem.app.controller.storage.SharedStorage.Theme
import com.yoavst.changesystemohelshem.BuildConfig
import com.yoavst.changesystemohelshem.R
import kotlinx.android.synthetic.main.settings_activity.*
import org.jetbrains.anko.*

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
        notificationsList.apply {
            settingsItem(getString(R.string.timetable_notif), showCheckBox = true, isChecked = storage.notificationsForTimetable) {
                storage.notificationsForTimetable = it
                OngoingNotificationService.update(this@SettingsActivity)
            }
            settingsItem(getString(R.string.changes), showCheckBox = true, isChecked = storage.notificationsForChanges) {
                storage.notificationsForChanges = it
                if (it)
                    analyticsManager.subscribe()
                else
                    analyticsManager.unsubscribe()
            }
            settingsItem(getString(R.string.birthdays), showCheckBox = true, isChecked = storage.notificationsForBirthdays) {
                storage.notificationsForBirthdays = it
            }
            settingsItem(getString(R.string.tests), showCheckBox = true, isChecked = storage.notificationsForTests) {
                storage.notificationsForTests = it
            }
            settingsItem(getString(R.string.holidays), showCheckBox = true, isChecked = storage.notificationsForHolidays) {
                storage.notificationsForHolidays = it
            }
        }


        themeList.apply {
            val options = stringArrayRes(R.array.night_mode_options)
            val flags = intArrayOf(AppCompatDelegate.MODE_NIGHT_NO, AppCompatDelegate.MODE_NIGHT_YES, AppCompatDelegate.MODE_NIGHT_AUTO)
            settingsItem(getString(R.string.dark_mode), items = options, default = flags.indexOf(storage.darkMode)) {
                storage.darkMode = flags[it]
            }
        }
        val currentMode = storage.theme
        val rippleColor = AttributeExtractor.extractRippleColorFrom(this)

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
        contactButton.onClick {
            email("yoav.sternberg@gmail.com", subject = getString(R.string.email_title))
        }
        exitButton.onClick {
            setResult(42)
            finish()
        }
        openSourceButton.onClick {
            browse("https://ohelshem.github.io/android")
        }
        facebookButton.onClick {
            browse("https://www.facebook.com/ohelshem.school")
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

        versionName.append(": " + BuildConfig.VERSION_NAME)
        versionCode.append(": " + BuildConfig.VERSION_CODE)
        versionType.append(": " + BuildConfig.BUILD_TYPE)
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
                    onCheckedChange { compoundButton, b -> listener(b) }
                }.lparams(width = wrapContent, height = matchParent)
                onClick { v -> (getChildAt(0) as CompoundButton).toggle() }
            } else onClick({ v -> listener(false) })
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

    fun LinearLayout.settingsItem(title: String, subtitle: String = "", items: Array<String>, default: Int, listener: ((selected: Int) -> Unit)): LinearLayout {
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
                    onItemSelected { adapterView, view, position, id ->
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


}