package com.ohelshem.app.android.changes

import com.ohelshem.app.android.main.ScreenManager

interface IBaseChangesPresenter {
    val changesDate: Long
    val hasData: Boolean

    fun update()
    fun refresh(screen: ScreenManager): Boolean
    fun launchTimetableScreen(screen: ScreenManager)
}