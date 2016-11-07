package com.ohelshem.app.android.utils

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import com.github.salomonbrys.kodein.LazyKodein
import com.github.salomonbrys.kodein.LazyKodeinAware
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.erased.instance
import com.ohelshem.app.controller.analytics.Analytics
import com.ohelshem.app.controller.storage.SharedStorage
import com.ohelshem.app.controller.storage.Storage
import com.yoavst.changesystemohelshem.R

abstract class AppThemedActivity: AppCompatActivity(), LazyKodeinAware {
    override val kodein = LazyKodein(appKodein)

    protected val storage: Storage by instance()
    protected val analytics: Analytics by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(storage.darkMode)
        val theme = storage.theme
        val themeId = when (theme) {
            SharedStorage.Theme.Red -> R.style.Theme_Red
            SharedStorage.Theme.Green -> R.style.Theme_Green
            SharedStorage.Theme.Blue ->  R.style.Theme_Blue
        }
        setTheme(themeId)
        super.onCreate(savedInstanceState)
    }
}