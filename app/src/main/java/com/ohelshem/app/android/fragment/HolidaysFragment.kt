package com.ohelshem.app.android.fragment

import android.support.v7.widget.LinearLayoutManager
import com.yoavst.changesystemohelshem.R
import com.ohelshem.app.android.adapter.HolidaysAdapter
import com.ohelshem.app.clearTime
import com.ohelshem.app.controller.TimetableController
import kotlinx.android.synthetic.main.holidays_fragment.*
import java.util.*
import java.util.concurrent.TimeUnit

class HolidaysFragment : BaseFragment() {
    override val layoutId: Int = R.layout.holidays_fragment

    override fun init() {
        val now = Calendar.getInstance().clearTime().timeInMillis
        daysToSummer.text = TimeUnit.MILLISECONDS.toDays(TimetableController.Summer.startTime - now).toString()
        val nextHoliday = TimetableController.Holidays.find { it.startTime >= now }
        if (nextHoliday == null)
            daysToHoliday.text = daysToSummer.text
        else {
            var days = TimeUnit.MILLISECONDS.toDays(nextHoliday.startTime - now)
            if (days < 0) days = 0
            daysToHoliday.text = days.toString()
        }
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = HolidaysAdapter()

    }
}