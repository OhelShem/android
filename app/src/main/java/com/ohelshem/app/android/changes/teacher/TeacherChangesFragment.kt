package com.ohelshem.app.android.changes.teacher

import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import com.github.salomonbrys.kodein.erased.instance
import com.ohelshem.api.model.Change
import com.ohelshem.app.android.changes.BaseChangesFragment
import com.ohelshem.app.android.changes.layer.LayerChangesAdapter
import com.ohelshem.app.android.drawableRes
import com.ohelshem.app.android.stringArrayRes
import com.ohelshem.app.android.stringResource
import com.ohelshem.app.getDay
import com.ohelshem.app.toCalendar
import com.yoavst.changesystemohelshem.R
import kotlinx.android.synthetic.main.changes_teacher_fragment.*
import org.jetbrains.anko.childrenSequence
import org.jetbrains.anko.onClick
import org.jetbrains.anko.padding
import java.text.SimpleDateFormat
import java.util.*

class TeacherChangesFragment : BaseChangesFragment<TeacherChangesPresenter>(), TeacherChangesView {
    override val layoutId: Int = R.layout.changes_teacher_fragment
    private val day by stringResource(R.string.day)
    private val weekDays by lazy { resources.getStringArray(R.array.week_days) }

    override fun createPresenter(): TeacherChangesPresenter = with(kodein()) { TeacherChangesPresenter(instance(), instance()) }

    override fun init() {
        screenManager.screenTitle = ""
        recyclerView.padding = 0
        recyclerView.layoutManager = GridLayoutManager(activity, MaxChangesHours + 1, LinearLayoutManager.HORIZONTAL, true)
        recyclerView.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.HORIZONTAL).apply { setDrawable(drawableRes(R.drawable.divider_white)!!) })
        recyclerView.setHasFixedSize(true)
        screenManager.setToolbarElevation(false)

        val children = layersBar.childrenSequence().toList()
        children.forEach {
            it.onClick {
                children.forEach { it.isSelected = false }
                it!!.isSelected = true
                presenter.onLayerSelected((it.tag as String).toInt())
            }
        }
    }

    override fun setSelectedLayer(layer: Int) {
        layersBar.childrenSequence().forEach { it.isSelected = false }
        layersBar.findViewWithTag(layer.toString()).isSelected = true
    }

    override fun showData(changes: List<Change>) {
        date.text = ChangesDataFormat.format(Date(presenter.changesDate))
        nameDay.text = "$day ${weekDays[presenter.changesDate.toCalendar().getDay() - 1]}"
        recyclerView.adapter = LayerChangesAdapter(context, changes, presenter.classesAtLayer, MaxChangesHours, stringArrayRes(R.array.layers)[presenter.currentLayer - 9])
        recyclerView.scrollToPosition(0)
    }

    companion object {
        private const val MaxChangesHours = 11
        private val ChangesDataFormat = SimpleDateFormat("dd/MM")
    }
}