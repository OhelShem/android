package com.ohelshem.app.android.changes

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.instance
import com.hannesdorfmann.mosby.mvp.layout.MvpLinearLayout
import com.ohelshem.api.model.Change
import com.ohelshem.api.model.UpdateError
import com.ohelshem.app.controller.timetable.TimetableController.Companion.DayType
import com.yoavst.changesystemohelshem.R
import org.jetbrains.anko.childrenSequence
import org.jetbrains.anko.matchParent

class LayerChangesView : MvpLinearLayout<ChangesView, LayerChangesPresenter>, ChangesView {
    private var hasInit: Boolean = false
    private lateinit var rows: List<LinearLayout>

    override fun createPresenter(): LayerChangesPresenter = with(appKodein()) { LayerChangesPresenter(instance(), instance(), instance()) }

    constructor(ctx: Context) : super(ctx)

    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)

    init {
        orientation = LinearLayout.HORIZONTAL
        isBaselineAligned = false
        addView(HorizontalScrollView(context).apply {
            isFillViewport = true
            overScrollMode = ScrollView.OVER_SCROLL_NEVER
            layoutParams = LinearLayout.LayoutParams(0, matchParent, 1f)
        })
        View.inflate(context, R.layout.layer_hours, this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        presenter.onCreate()
    }

    override fun onError(error: UpdateError) = Unit

    override fun onEmptyData(dayType: DayType) = Unit

    override fun setData(changes: List<Change>) {
        if (visibility == View.VISIBLE || hasInit) {
            if (!hasInit) {
                initLayout()
            }
            this.changes = null
            LayerChangesGenerator.fillTable(changes, presenter.classesAtLayer, rows)
        } else {
            this.changes = changes
        }
    }

    private var changes: List<Change>? = null

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == View.VISIBLE) {
            changes?.let { setData(it) }
        }
    }

    private fun initLayout() {
        val view = LayerChangesGenerator.createView(context, presenter.classesAtLayer, presenter.userLayer)
        (getChildAt(0) as ViewGroup).addView(view)
        this.rows = view.childrenSequence().drop(1).map { it as LinearLayout }.toList()
    }

    override val isShowingData: Boolean
        get() = presenter.hasData
}