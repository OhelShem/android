package com.ohelshem.app.android.holidays.calendar


import android.content.res.Configuration
import com.ohelshem.app.android.hide
import com.ohelshem.app.android.holidays.HolidaysPresenter
import com.ohelshem.app.android.holidays.HolidaysView
import com.ohelshem.app.android.show
import com.ohelshem.app.android.utils.BaseMvpFragment
import com.ohelshem.app.android.utils.view.OneDayDecorator
import com.ohelshem.app.controller.timetable.TimetableController
import com.ohelshem.app.controller.timetable.TimetableController.Companion.Holiday
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.yoavst.changesystemohelshem.R
import kotlinx.android.synthetic.main.calendar_fragment.*
import java.util.*

class HolidaysCalendarFragment : BaseMvpFragment<HolidaysView, HolidaysPresenter>(), HolidaysView {
    override val layoutId: Int = R.layout.calendar_fragment
    override fun createPresenter(): HolidaysPresenter = HolidaysPresenter()

    private var dates: Array<Holiday> = emptyArray()

    override fun init() {
        calendarView.currentDate = CalendarDay.today()
        calendarView.isPagingEnabled = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        calendarView.state().edit().apply {
            setMinimumDate(CalendarDay.from(TimetableController.StartOfTheYear))
            setMaximumDate(CalendarDay.from(Date(TimetableController.Summer.endTime)))
        }.commit()
        calendarView.setOnDateChangedListener { materialCalendarView, calendarDay, selected ->
            val time = calendarDay.date.time
            for (holiday in dates) {
                if (holiday.isOneDay()) {
                    if (time == holiday.startTime) {
                        update(holiday)
                        break
                    }
                } else {
                    if (time >= holiday.startTime && time <= holiday.endTime) {
                        update(holiday)
                        break
                    }
                }
            }
        }
    }

    private fun update(holiday: Holiday?) {
        if (holiday != null) {
            indicator.hide()
            data.show()
            title.text = holiday.name
            if (holiday.isOneDay())
                extra.text = holiday.start
            else
                extra.text = holiday.start + " - " + holiday.end
        } else clear()
    }

    private fun clear() {
        title.text = ""
        extra.text = ""
    }

    override fun showHolidays(holidays: Array<Holiday>, summer: Holiday) {
        dates = holidays + summer
        calendarView.removeDecorators()
        calendarView.addDecorator(HolidayDecorator.generate(context, holidays))
        calendarView.addDecorator(OneDayDecorator())
    }


}

