package com.ohelshem.app.android.timetable

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import com.ohelshem.api.model.Hour
import com.ohelshem.app.android.timetable.adapter.TimetableAdapter
import org.jetbrains.anko.dip
import org.jetbrains.anko.padding

class TimetableDayView : RecyclerView, TimetableBasicView {
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    init {
        padding = dip(16)
        setHasFixedSize(true)
        clipToPadding = false
        layoutManager = LinearLayoutManager(context)
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
        adapter = TimetableAdapter(context, data[day]) { hour, position ->
            onClickListener?.invoke(day, position, hour)
        }
    }

    override fun destroyView() {
        adapter = null
    }

}