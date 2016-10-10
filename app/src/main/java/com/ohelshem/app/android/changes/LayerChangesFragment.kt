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

package com.ohelshem.app.android.changes

import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.LinearLayout.HORIZONTAL
import android.widget.LinearLayout.VERTICAL
import android.widget.TextView
import com.github.salomonbrys.kodein.instance
import com.ohelshem.api.model.Change
import com.ohelshem.app.android.*
import com.yoavst.changesystemohelshem.R
import kotlinx.android.synthetic.main.layer_changes_fragment.*
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.act
import org.jetbrains.anko.support.v4.dip

class LayerChangesFragment : BaseChangesFragment<LayerChangesPresenter>() {
    override val layoutId: Int = R.layout.layer_changes_fragment

    override fun createPresenter(): LayerChangesPresenter = with(kodein()) { LayerChangesPresenter(instance(), instance(), instance()) }

    private val layerText: String by lazy { stringArrayRes(R.array.layers)[presenter.userLayer - 9] }

    private var hasInitTable = false
    private lateinit var rows: List<LinearLayout>

    override fun init() {
        screenManager.setToolbarElevation(false)
    }

    override fun showData(changes: List<Change>) {
        fillTable(changes)
    }

    private fun initLayout() {
        val classes = presenter.classesAtLayer

        val headerRowHeight = dip(30)
        val defaultCellMargin = dip(1)
        val standardColumnWidth = screenSize.x / 6

        val rows: MutableList<LinearLayout> = mutableListOf()

        with(tableScrollView) {
            linearLayout {
                gravity = Gravity.RIGHT
                orientation = VERTICAL

                // Header row:
                linearLayout {
                    orientation = HORIZONTAL
                    backgroundColor = act.primaryColor

                    repeat(classes) { c ->
                        val clazz = classes - c
                        autoResizeTextView {
                            gravity = Gravity.CENTER
                            textColor = Color.WHITE
                            padding = 10
                            text = "$layerText'$clazz"

                            setTypeface(null, Typeface.BOLD)
                        }.lparams(width = standardColumnWidth, height = matchParent) {
                            marginize(clazz, classes, 0, MaxChangeHours, defaultCellMargin)
                        }

                    }
                }.lparams(width = matchParent, height = headerRowHeight)

                // Changes rows
                repeat(MaxChangeHours) { hour ->
                    rows += linearLayout {
                        orientation = HORIZONTAL

                        repeat(classes) { c ->
                            val clazz = classes - c
                            autoResizeTextView {
                                gravity = Gravity.CENTER
                                textColor = Color.WHITE
                                backgroundColor = NoChangesColors[hour % 2]
                                padding = 10

                            }.lparams(width = standardColumnWidth, height = matchParent) {
                                marginize(clazz, classes, hour, MaxChangeHours, defaultCellMargin)
                            }
                        }

                    }.lparams(width = matchParent, height = 0) {
                        weight = 1f
                    }
                }
            }
        }
        this.rows = rows
    }

    private fun ViewGroup.MarginLayoutParams.marginize(clazz: Int, maxClasses: Int, hour: Int, maxHours: Int, margin: Int) {
        if (hour != 0) {
            topMargin = margin
        }
        if (hour != maxHours - 1) {
            bottomMargin = margin
        }

        if (clazz != maxClasses) {
            leftMargin = margin
        }
        if (clazz != 0) {
            rightMargin = margin
        }
    }

    fun fillTable(changes: List<Change>) {
        val classes = presenter.classesAtLayer

        if (!hasInitTable) {
            initLayout()
            hasInitTable = true
        }

        rows.forEachIndexed { hour, row ->
            row.childrenSequence().forEach {
                it as TextView
                it.backgroundColor = NoChangesColors[hour % 2]
                it.text = ""
            }
        }

        changes.forEach {
            (rows[it.hour - 1][classes - it.clazz] as TextView).apply {
                backgroundColor = it.color
                text = it.content
            }
        }

        tableScrollView.post {
            tableScrollView?.fullScroll(HorizontalScrollView.FOCUS_RIGHT)
        }

    }

    companion object {
        private val NoChangesColors = intArrayOf(Color.parseColor("#D9D9D9"), Color.parseColor("#BABABA"))
        private const val MaxChangeHours = 10
    }
}