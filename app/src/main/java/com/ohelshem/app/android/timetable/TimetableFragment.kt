/*
 * Copyright 2016 Yoav Sternberg.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.ohelshem.app.android.timetable

import android.support.v7.app.AlertDialog
import android.view.*
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import com.github.salomonbrys.kodein.erased.factory
import com.github.salomonbrys.kodein.erased.instance
import com.ohelshem.api.model.Hour
import com.ohelshem.api.model.SchoolHour
import com.ohelshem.app.android.hide
import com.ohelshem.app.android.settings.OverridesActivity
import com.ohelshem.app.android.timetable.adapter.DaySpinnerAdapter
import com.ohelshem.app.android.utils.BaseMvpFragment
import com.ohelshem.app.controller.storage.TeacherStorage
import com.ohelshem.app.controller.timetable.TimetableController
import com.ohelshem.app.model.WrappedHour
import com.yoavst.changesystemohelshem.R
import kotlinx.android.synthetic.main.timetable_fragment.*
import org.jetbrains.anko.find
import org.jetbrains.anko.sdk15.listeners.onItemSelectedListener
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.longToast
import org.jetbrains.anko.support.v4.toast

class TimetableFragment : BaseMvpFragment<TimetableView, TimetablePresenter>(), TimetableView {
    override val layoutId: Int = R.layout.timetable_fragment
    private lateinit var menuEdit: MenuItem
    private lateinit var menuDone: MenuItem

    override fun createPresenter(): TimetablePresenter = with(kodein()) {
        val teacherStorage = instance<TeacherStorage>()
        val timetableControllerWrapper = factory<List<SchoolHour>, TimetableController>()
        TimetablePresenter(instance(), instance(),
                { layer: Int, clazz: Int -> timetableControllerWrapper(teacherStorage.getTimetableForClass(layer, clazz)) },
                isEditModeSupported = true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        menuEdit = menu.findItem(R.id.edit)
        menuEdit.setOnMenuItemClickListener {
            if (presenter.isEditModeSupported) {
                toast(R.string.start_edit_mode)
                it.isVisible = false
                menuDone.isVisible = true
                presenter.isEditModeEnabled = true
            } else toast(R.string.edit_mode_not_supported)
            true
        }
        menuDone = menu.findItem(R.id.done)

        menuDone.setOnMenuItemClickListener {
            toast(R.string.finish_edit_mode)
            presenter.isEditModeEnabled = false
            disableEditMode()
            true
        }

        menu.findItem(R.id.overridesSettings).setOnMenuItemClickListener {
            activity.startActivity<OverridesActivity>()
            true
        }
    }

    override fun disableEditMode() {
        menuDone.isVisible = false
        menuEdit.isVisible = true
    }

    override fun init() {
        timetableLayout.onClickListener = { day, hour, data ->
            if (presenter.isEditModeEnabled)
                presenter.startEdit(data, day, hour)
            else if (presenter.groupFormatting && data.teacher.count { it == ',' } >= 2)
                if (presenter.currentDay == TimetableLayout.Day_Week) longToast(data.teacher)
        }
    }

    override fun setDay(day: Int, data: Array<Array<Hour>>) {
        removeListener()
        screenManager.topNavigationElement.setSelection(day, false)
        setListener()
        screenManager.setToolbarElevation(day != TimetableLayout.Day_Week)
        timetableLayout.setData(data, day, presenter.groupFormatting)

    }

    override fun flush() {
        timetableLayout.destroyView()
    }

    override fun showEditScreen(hour: Hour, day: Int, position: Int, hasOverride: Boolean) {
        val room = (hour as? WrappedHour)?.room?.takeIf { it != 0 }

        val view = View.inflate(activity, R.layout.dialog_override, null)
        view.find<TextView>(R.id.currentName).text = hour.name
        view.find<TextView>(R.id.currentTeacher).text = hour.teacher
        if (room != null)
            view.find<TextView>(R.id.currentRoom).text = "$room"
        else {
            view.find<TextView>(R.id.currentRoom).hide()
            view.find<TextView>(R.id.currentRoomTitle).hide()

        }


        val newName = view.find<EditText>(R.id.newName)
        val newTeacher = view.find<EditText>(R.id.newTeacher)
        val newRoom = view.find<EditText>(R.id.newRoom)

        val all = view.find<CheckBox>(R.id.changeAll)
        if (hour is WrappedHour) {
            newName.hint = hour.oldName
            newTeacher.hint = hour.oldTeacher
        }
        val builder = AlertDialog.Builder(activity)
                .setView(view)
                .setPositiveButton(R.string.apply) { _, _ ->
                    presenter.edit(hour, day, position, newName.text.toString(), newTeacher.text.toString(), newRoom.text.toString().toIntOrNull() ?: -1, all.isChecked)
                }
                .setNegativeButton(R.string.cancel) { _, _ ->

                }
        if (hasOverride) {
            builder.setNeutralButton(R.string.return_to_default) { _, _ ->
                presenter.returnToDefault(hour, day, position, all.isChecked)
            }
        }
        builder.show()
    }

    override fun flushMenu() {
        val spinner = screenManager.topNavigationElement
        spinner.adapter = DaySpinnerAdapter(activity, presenter.daysLearning)
        spinner.gravity = Gravity.CENTER
        spinner.post {
            setListener()
        }
    }

    private fun removeListener() {
        screenManager.topNavigationElement.onItemSelectedListener {}
    }

    private fun setListener() {
        screenManager.topNavigationElement.onItemSelectedListener {
            onItemSelected { _, _, position, _ ->
                presenter.setDay(position)
            }
        }
    }

    override val isShowingDayView: Boolean
        get() = timetableLayout.isVisible

    override var menuId: Int = R.menu.timetable


}