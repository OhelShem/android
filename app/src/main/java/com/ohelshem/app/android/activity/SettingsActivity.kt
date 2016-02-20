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

package com.ohelshem.app.android.activity

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.widget.CompoundButton
import android.widget.LinearLayout
import com.yoavst.changesystemohelshem.BuildConfig
import com.yoavst.changesystemohelshem.R
import com.ohelshem.app.android.appCompatSwitch
import com.ohelshem.app.android.resourceAttr
import com.ohelshem.app.android.service.OngoingNotificationService
import com.ohelshem.app.android.show
import com.ohelshem.app.controller.DBController
import kotlinx.android.synthetic.main.settings_activity.*
import org.jetbrains.anko.*
import uy.kohesive.injekt.injectLazy

class SettingsActivity : AppCompatActivity() {
    val databaseController: DBController by injectLazy()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        initLayout()
        initViews()
    }

    private fun initLayout() {
        settingsList.apply {
            settingsItem(getString(R.string.changes), showCheckBox = true, isChecked = databaseController.notificationsForChangesEnabled) {
                databaseController.notificationsForChangesEnabled = it
            }
            settingsItem(getString(R.string.tests), showCheckBox = true, isChecked = databaseController.notificationsForTestsEnabled) {
                databaseController.notificationsForTestsEnabled = it
            }
            settingsItem(getString(R.string.holidays), showCheckBox = true, isChecked = databaseController.notificationsForHolidaysEnabled) {
                databaseController.notificationsForHolidaysEnabled = it
            }
            settingsItem(getString(R.string.timetable), showCheckBox = true, isChecked = databaseController.notificationsForTimetableEnabled) {
                databaseController.notificationsForTimetableEnabled = it
                OngoingNotificationService.update(this@SettingsActivity)
            }
        }
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
            browse("http://www.ohel-shem.com/portal4/index.php")
        }
        overridesButton.onClick {
            startActivity<OverridesActivity>()
        }
        versionName.append(": " + BuildConfig.VERSION_NAME)
        versionCode.append(": " + BuildConfig.VERSION_CODE)
        versionType.append(": " + BuildConfig.BUILD_TYPE)
        if (databaseController.developerModeEnabled)
            developerCard.show()
        openGameMode.onClick {
            startActivity<GuessingActivity>()
        }
        versionCode.onClick {
            if (!databaseController.developerModeEnabled) {
                numberOfTaps++
                handler.removeCallbacksAndMessages(null)
                if (numberOfTaps == 7) {
                    databaseController.developerModeEnabled = true
                    developerCard.show()
                } else handler.postDelayed(request, 500)
            }
        }

    }

    private var numberOfTaps: Int = 0
    private val handler = Handler()
    private val request: Runnable = Runnable {
        numberOfTaps = 0
    }

    fun LinearLayout.settingsItem(title: String, subtitle: String = "", showCheckBox: Boolean = false, isChecked: Boolean = false, listener: ((checked: Boolean) -> Unit) = { b -> }): LinearLayout {
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


}