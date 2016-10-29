package com.ohelshem.app.android.dates.list

import com.ohelshem.app.android.utils.BasePresenter
import com.ohelshem.app.controller.timetable.TimetableController

class HolidaysPresenter: BasePresenter<HolidaysView>() {
    override fun onCreate() {
        view?.showHolidays(TimetableController.Holidays, TimetableController.Summer)
    }
    override fun onDestroy() = Unit
}