package com.ohelshem.app.controller.timetable

import com.ohelshem.api.controller.implementation.ApiParserImpl
import com.ohelshem.api.model.Hour
import com.ohelshem.api.model.SchoolHour

class SchoolHourTimetableController(hours: List<SchoolHour>) : BaseTimetableController() {
    override fun init() = Unit

    init {
        val isLearnOnFriday = hours.find { it.day == 5 } != null
        val timetable = Array<Array<Hour?>>(if (isLearnOnFriday) 6 else 5) { arrayOfNulls(ApiParserImpl.MaxHoursADay) }
        hours.forEach {
            timetable[it.day][it.hour] = it
        }
        timetable.forEach { day ->
            day.indices
                    .filter { day[it] == null }
                    .forEach { day[it] = Hour(" ", " ", TimetableController.ColorEmpty) }
        }

        this.timetable = Array(timetable.size) {
            @Suppress("UNCHECKED_CAST")
            (timetable[it] as Array<Hour>).dropLastWhile(Hour::isEmpty).toTypedArray()
        }
    }
}