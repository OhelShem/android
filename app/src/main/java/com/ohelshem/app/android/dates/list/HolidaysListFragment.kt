package com.ohelshem.app.android.dates.list

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.ohelshem.app.android.utils.BaseMvpFragment
import com.ohelshem.app.clearTime
import com.ohelshem.app.controller.timetable.TimetableController.Companion.Holiday
import com.yoavst.changesystemohelshem.R
import java.util.*

class HolidaysListFragment : BaseMvpFragment<HolidaysView, HolidaysPresenter>(), HolidaysView {
    override val layoutId: Int = R.layout.list
    override fun createPresenter(): HolidaysPresenter = HolidaysPresenter()

    lateinit var list: RecyclerView

    override fun init() {
        list = view as RecyclerView

        list.layoutManager = LinearLayoutManager(activity)
        list.setHasFixedSize(true)
    }

    override fun showHolidays(holidays: Array<Holiday>, summer: Holiday) {
        list.adapter = HolidaysAdapter(holidays)

        val now = Date().clearTime().time

        val position = holidays.indexOfFirst { it.startTime <= now && now <= it.endTime } indexOr { holidays.indexOfFirst { it.startTime > now } }
        if (position >= 1)
            list.scrollToPosition(position)
    }

    private inline infix fun Int.indexOr(generator: () -> Int) = if (this == -1) generator() else this
}