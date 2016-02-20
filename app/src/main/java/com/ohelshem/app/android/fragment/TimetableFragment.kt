/*
 * Copyright 2016 Yoav Sternberg.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.ohelshem.app.android.fragment

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.*
import com.yoavst.changesystemohelshem.R
import com.ohelshem.app.android.adapter.DaySpinnerAdapter
import com.ohelshem.app.android.adapter.TimetableAdapter
import com.ohelshem.app.android.colorRes
import com.ohelshem.app.android.hide
import com.ohelshem.app.android.show
import com.ohelshem.app.controller.DBController
import com.ohelshem.app.controller.TimetableController
import com.ohelshem.app.getDay
import com.ohelshem.app.getHour
import com.ohelshem.app.model.ApiUpdatable
import com.ohelshem.app.model.OverrideData
import com.ohelshem.app.model.WrappedHour
import com.ohelshem.api.controller.declaration.ApiController
import com.ohelshem.api.model.Hour
import com.ohelshem.api.model.UpdateError
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.customView
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.dip
import org.jetbrains.anko.support.v4.toast
import uy.kohesive.injekt.injectLazy
import java.util.*
import kotlin.properties.Delegates

/**
 * The fragment that is responsible on showing the changes.
 */
class TimetableFragment : BaseFragment(), ApiUpdatable<Array<Array<Hour>>> {
    override var menuId: Int = R.menu.timetable
    private val timetableController: TimetableController by injectLazy()
    private val databaseController: DBController by injectLazy()
    private val learnsOnFriday by lazy { timetableController.learnsOnFriday }
    private val weekDays by lazy { if (learnsOnFriday) 5 else 4 }
    private var hasInitAllWeek = false
    private var isInEditMode = false
    private lateinit var recyclerView: RecyclerView
    private lateinit var headerView: ViewGroup
    private lateinit var table: TableLayout
    private lateinit var allWeek: ViewGroup
    private lateinit var menuEdit: MenuItem
    private lateinit var menuDone: MenuItem


    private var day: Int by Delegates.observable(-1) { property, oldValue, newValue ->
        if (newValue == 0) {
            if (recyclerView.adapter != null)
                recyclerView.adapter = null
            recyclerView.hide()
            if (!hasInitAllWeek) initTimetable()
            allWeek.show()

        } else {
            recyclerView.show()
            allWeek.hide()
            recyclerView.adapter = TimetableAdapter(activity, timetableController[newValue - 1]) { hour, position ->
                if (isInEditMode) edit(hour, day - 1, position)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = UI {
        frameLayout {
            recyclerView = customView<RecyclerView> {
                padding = dip(16)
                setHasFixedSize(true)
                clipToPadding = false
                layoutManager = LinearLayoutManager(activity)
            }
            allWeek = include<LinearLayout>(R.layout.timetable_all_week) {
                headerView = find(R.id.header_row)
                if (!learnsOnFriday) headerView.removeViewAt(0)
                table = find(R.id.table)
                visibility = View.GONE
            }
        }
    }.view

    override fun init() {
        // Now the dark magic, automatically setting a date!
        if (databaseController.isCacheUpdated()) {
            val nextLessons = timetableController.getHourData()
            day = nextLessons.hour.day + 1
        } else {
            var temp = Calendar.getInstance().getDay()
            if (getHour() >= 21) temp++
            if (temp > weekDays + 1)
                temp = 1
            day = temp
        }
        val spinner = drawerActivity.getNavigationSpinner()
        spinner.adapter = DaySpinnerAdapter(activity, weekDays + 1)
        spinner.setSelection(day)
        spinner.onItemSelectedListener {
            onItemSelected { adapterView, view, position, id ->
                day = position
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menuEdit = menu.findItem(R.id.edit)
        menuEdit.setOnMenuItemClickListener {
            toast(R.string.start_edit_mode)
            it.isVisible = false
            menuDone.isVisible = true
            isInEditMode = true
            true
        }
        menuDone = menu.findItem(R.id.done)

        menuDone.setOnMenuItemClickListener {
            toast(R.string.finish_edit_mode)
            it.isVisible = false
            menuEdit.isVisible = true
            isInEditMode = false
            true
        }
    }

    override fun onUpdate(data: Array<Array<Hour>>) {
        onUpdate()
    }

    override fun onError(error: UpdateError) {
        // ignored
    }

    override val api: ApiController.Api = ApiController.Api.Timetable

    override fun onUpdate() {
        init()
    }

    private fun edit(hour: Hour, day: Int, position: Int) {
        val view = View.inflate(activity, R.layout.dialog_override, null)
        view.find<TextView>(R.id.currentName).text = hour.name
        view.find<TextView>(R.id.currentTeacher).text = hour.teacher
        val newName = view.find<EditText>(R.id.newName)
        val newTeacher = view.find<EditText>(R.id.newTeacher)
        val all = view.find<CheckBox>(R.id.changeAll)
        if (hour is WrappedHour) {
            newName.hint = hour.oldName
            newTeacher.hint = hour.oldTeacher
        }
        AlertDialog.Builder(activity)
                .setView(view)
                .setPositiveButton(R.string.apply) { dialog, which ->
                    if (newName.text.isEmpty() && newTeacher.text.isEmpty()) {
                        /*if ((newName.hint?.length() ?: 0) != 0)
                            override(day, position, newName.hint.toString(), isRecyclerView)*/
                    } else {
                        val text = newName.text.toString()
                        val teacher = newTeacher.text.toString()
                        if (!all.isChecked) {
                            var overrides = databaseController.overrides
                            val index = overrides.indexOfFirst { it.day == day && it.hour == position }
                            val override = OverrideData(day, position, text, teacher)
                            if (index < 0) overrides += override
                            else overrides[index] = override
                            databaseController.overrides = overrides
                        } else {
                            val overrides = databaseController.overrides.toCollection(LinkedList())
                            val name = hour.name
                            for (dayOfWeek in 0 until timetableController.size) {
                                timetableController[dayOfWeek].forEachIndexed { i, hour ->
                                    if (hour.name == name) {
                                        val index = overrides.indexOfFirst { it.day == dayOfWeek && it.hour == i }
                                        val override = OverrideData(dayOfWeek, i, text, teacher)
                                        if (index < 0) overrides += override
                                        else overrides[index] = override
                                    }
                                }
                            }
                            databaseController.overrides = overrides.toTypedArray()
                        }
                        updateAllWeek()
                        this.day = this.day // force refresh
                    }


                }
                .setNegativeButton(R.string.cancel) { dialog, which ->

                }.show()
    }

    private fun updateAllWeek() {
        if (hasInitAllWeek) {
            table.removeAllViews()
            hasInitAllWeek = false
        }
    }


    private fun initTimetable() {
        hasInitAllWeek = true
        var max = (0..weekDays).map { timetableController[it].size }.max()!!
        val dp1 = dip(1)
        val dp24 = dip(24)
        val dp30 = dip(30)
        for (i in 0..max - 1) {
            val tableRow = TableRow(activity)
            val tableParams = TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            tableParams.setMargins(dp1, 0, 0, dp1)
            tableRow.layoutParams = tableParams
            table.addView(tableRow)
            for (d in weekDays downTo 0) {
                val view = activity.layoutInflater.inflate(R.layout.timetable_weekly_item, tableRow, false)
                view.id = getId(d, i)
                if (timetableController[d].size == 0) {
                    continue
                } else if (timetableController[d].size <= i) {
                    view.setBackgroundColor(Color.TRANSPARENT)
                } else {
                    view.setBackgroundColor(timetableController[d, i].color)
                    (view.findViewById(R.id.text) as TextView).text = timetableController[d, i].name
                }
                (view.layoutParams as TableRow.LayoutParams).setMargins(0, 0, dp1, 0)
                view.onClick {
                    if (isInEditMode)
                        edit(timetableController[d, i], d, i)
                }
                tableRow.addView(view)
            }
            val frameLayout = FrameLayout(activity)
            frameLayout.layoutParams = TableRow.LayoutParams(dp30, ViewGroup.LayoutParams.MATCH_PARENT)
            frameLayout.setBackgroundColor(activity.colorRes(R.color.colorPrimary))
            val number = TextView(activity)
            number.layoutParams = FrameLayout.LayoutParams(dp24, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER)
            number.textSize = 15f
            number.setTypeface(null, Typeface.BOLD)
            number.setTextColor(Color.WHITE)
            number.gravity = Gravity.CENTER
            number.text = (i + 1).toString()
            frameLayout.addView(number)
            tableRow.addView(frameLayout)
        }
    }

    private fun getId(day: Int, hour: Int) = 100 + day * 10 + hour
}