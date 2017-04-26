package com.ohelshem.app.android.dates.list

import com.hannesdorfmann.mosby3.mvp.MvpView
import com.ohelshem.app.controller.timetable.TimetableController.Companion.Holiday

interface HolidaysView: MvpView {
    fun showHolidays(holidays: Array<Holiday>, summer: Holiday)
}