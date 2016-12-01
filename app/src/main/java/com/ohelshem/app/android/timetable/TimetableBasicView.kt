package com.ohelshem.app.android.timetable

import com.ohelshem.api.model.Hour

interface TimetableBasicView {
    fun show()
    fun hide()
    val isVisible: Boolean

    fun setData(data: Array<Array<Hour>>, day: Int, groupFormatting: Boolean)

    fun destroyView()

    var onClickListener: ((day: Int, hour: Int, data: Hour) -> Unit)?
}