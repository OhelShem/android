package com.ohelshem.app.android.timetable

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.ohelshem.api.model.Hour

class TimetableLayout : FrameLayout, TimetableBasicView {
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    private var weekView: TimetableBasicView = TimetableWeekView(context, null)
    private var dayView: TimetableBasicView = TimetableDayView(context, null)

    init {
        weekView.onClickListener = { a, b, c -> onClickListener?.invoke(a, b, c) }
        dayView.onClickListener = { a, b, c -> onClickListener?.invoke(a, b, c) }

        addView(dayView as View)
        addView(weekView as View)

    }

    override var onClickListener: ((Int, Int, Hour) -> Unit)? = null


    override fun show() {
        visibility = View.VISIBLE
    }

    override fun hide() {
        visibility = View.GONE
    }

    override val isVisible: Boolean
        get() = visibility == View.VISIBLE

    override fun setData(data: Array<Array<Hour>>, day: Int, groupFormatting: Boolean) {
       if (day != Day_Week) {
           weekView.hide()
           dayView.show()
           dayView.setData(data, day - 1, groupFormatting)
       } else {
           dayView.hide()
           weekView.show()
           weekView.setData(data, day, groupFormatting)
       }
    }

    override fun destroyView() {
        dayView.destroyView()
        weekView.destroyView()
    }

    companion object {
        const val Day_Week = 0
    }
}