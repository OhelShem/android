package com.ohelshem.app.android.utils.view

import android.graphics.Typeface
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

/**
 * Decorate a day by making the text big and bold
 */
class OneDayDecorator(private val date: CalendarDay = CalendarDay.today()) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean = day == date

    override fun decorate(view: DayViewFacade) {
        view.addSpan(StyleSpan(Typeface.BOLD))
        view.addSpan(RelativeSizeSpan(1.4f))
    }
}