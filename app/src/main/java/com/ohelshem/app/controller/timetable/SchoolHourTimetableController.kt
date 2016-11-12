package com.ohelshem.app.controller.timetable

import com.ohelshem.api.controller.implementation.ApiParserImpl
import com.ohelshem.api.model.Hour
import com.ohelshem.api.model.SchoolHour

class SchoolHourTimetableController(hours: List<SchoolHour>) : BaseTimetableController() {

    override fun init() {

    }

    init {
        val isLearnOnFriday = hours.find { it.day == 6 } != null
        val timetable = Array<Array<Hour?>>(if (isLearnOnFriday) 6 else 5) { arrayOfNulls(ApiParserImpl.MaxHoursADay) }
        hours.forEach {
            timetable[it.day - 1][it.hour - 1] = it
        }
        timetable.forEach { day ->
            for (hour in day.indices) {
                if (day[hour] == null) day[hour] = Hour("", "", TimetableController.ColorEmpty)
            }
        }
        @Suppress("UNCHECKED_CAST")
        this.timetable = timetable as Array<Array<Hour>>
    }
}