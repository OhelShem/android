package com.ohelshem.app.android.dates.calendar

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import com.ohelshem.app.android.*
import com.ohelshem.app.controller.timetable.TimetableController.Companion.Holiday
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class HolidayDecorator(private val drawable: Drawable, val dates: Array<Holiday>) : DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay): Boolean {
        val time = day.date.time

        for (holiday in dates) {
            if (holiday.isOneDay()) {
                if (time == holiday.startTime) return true
            } else {
                if (time >= holiday.startTime && time <= holiday.endTime) return true
            }
        }
        return false
    }

    override fun decorate(view: DayViewFacade) {
        view.setBackgroundDrawable(drawable)
    }

    companion object {
        fun generate(context: Context, dates: Array<Holiday>): HolidayDecorator = HolidayDecorator(createDrawable(context), dates)

        private fun createDrawable(context: Context): Drawable {
            val colorNormal = context.primaryLightColor
            val colorSelected = context.primaryDarkColor
            val colorPressed = context.primaryColor
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                getPressedColorRippleDrawable(colorNormal, colorSelected, colorPressed)
            else
                getStateDrawable(colorNormal, colorSelected, colorPressed)
        }
    }
}