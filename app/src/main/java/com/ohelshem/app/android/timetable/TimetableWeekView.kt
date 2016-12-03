package com.ohelshem.app.android.timetable

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.ohelshem.api.model.Hour
import com.ohelshem.app.android.fromHtml
import com.ohelshem.app.android.hide
import com.ohelshem.app.android.primaryColor
import com.ohelshem.app.android.show
import com.yoavst.changesystemohelshem.R
import org.jetbrains.anko.*

class TimetableWeekView : LinearLayout, TimetableBasicView {
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    private var groupText: String

    override var onClickListener: ((day: Int, hour: Int, data: Hour) -> Unit)? = null

    private var headerView: ViewGroup
    private var table: TableLayout

    private var hasInit = false

    init {
        orientation = VERTICAL

        View.inflate(context, R.layout.timetable_all_week, this)
        headerView = find(R.id.header_row)
        table = find(R.id.table)

        groupText = "(" + context.getString(R.string.group) + ")"

    }

    override fun show() {
        visibility = View.VISIBLE
    }

    override fun hide() {
        visibility = View.GONE
    }

    override val isVisible: Boolean
        get() = visibility == View.VISIBLE

    override fun setData(data: Array<Array<Hour>>, day: Int, groupFormatting: Boolean) {
        if (!hasInit || table.childCount == 0)
            initView(data, groupFormatting)
    }

    override fun destroyView() {
        if (hasInit) {
            table.removeAllViews()
            hasInit = false
        }
    }

    private fun initView(data: Array<Array<Hour>>, groupFormatting: Boolean) {
        val max = data.map { it.size }.max()!!
        val dp1 = dip(1)
        val dp24 = dip(24)
        val dp30 = dip(30)
        val primaryColor = context.primaryColor
        val layoutInflater = context.layoutInflater

        //region Header view
        val days = BooleanArray(6)
        (0 until data.size)
                .filter { data[it].isNotEmpty() }
                .forEach { days[5 - it] = true }

        days.forEachIndexed { index, isLearning ->
            if (isLearning)
                headerView.getChildAt(index).show()
            else
                headerView.getChildAt(index).hide()
        }
        //endregion

        repeat(max) { hour ->
            val tableRow = TableRow(context).apply {
                layoutParams = TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    setMargins(dp1, 0, 0, dp1)
                }
            }

            table.addView(tableRow)

            (data.size - 1 downTo 0)
                    .filterNot { data[it].isEmpty() }
                    .map { day ->
                        layoutInflater.inflate(R.layout.timetable_weekly_item, tableRow, false).apply {
                            id = getId(day, hour)

                            if (data[day].size <= hour) {
                                backgroundColor = Color.TRANSPARENT
                                onClick {}
                            } else {
                                backgroundColor = data[day][hour].color
                                val current = data[day][hour]

                                onClick {
                                    onClickListener?.invoke(day, hour, current)
                                }
                                find<TextView>(R.id.text).text = generateText(current, groupFormatting)
                            }

                            (layoutParams as TableRow.LayoutParams).setMargins(0, 0, dp1, 0)
                        }
                    }
                    .forEach { tableRow.addView(it) }

            val frameLayout = FrameLayout(context).apply {
                layoutParams = TableRow.LayoutParams(dp30, ViewGroup.LayoutParams.MATCH_PARENT)
                backgroundColor = primaryColor
            }

            val number = TextView(context).apply {
                layoutParams = FrameLayout.LayoutParams(dp24, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER)
                textSize = 15f
                gravity = Gravity.CENTER
                text = (hour + 1).toString()
                textColor = Color.WHITE

                setTypeface(null, Typeface.BOLD)
            }

            frameLayout.addView(number)
            tableRow.addView(frameLayout)
        }

        hasInit = true
    }

    private fun generateText(hour: Hour, groupFormatting: Boolean): CharSequence {
        val mikbatz = groupFormatting && hour.teacher.count { it == ',' } >= 2
        return if (hour.name.isEmpty()) "" else if (groupFormatting) ("<b>${hour.name}</b> <font color='#ECEFF1'>${if (mikbatz) groupText else hour.teacher}</font>").fromHtml() else hour.name
    }

    private fun getId(day: Int, hour: Int) = 100 + day * 10 + hour

}