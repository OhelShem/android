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
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import com.github.salomonbrys.kodein.instance
import com.ohelshem.api.model.Change
import com.ohelshem.app.android.drawableRes
import com.ohelshem.app.android.primaryColor
import com.ohelshem.app.android.screenSize
import com.ohelshem.app.android.stringArrayRes
import com.yoavst.changesystemohelshem.R
import kotlinx.android.synthetic.main.layer_changes_fragment.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundDrawable
import org.jetbrains.anko.padding
import org.jetbrains.anko.textColor

class LayerChangesFragment : BaseChangesFragment<LayerChangesPresenter>() {
    override val layoutId: Int = R.layout.layer_changes_fragment

    override fun createPresenter(): LayerChangesPresenter = with(kodein()) { LayerChangesPresenter(instance(), instance(), instance()) }

    private val layerText: String by lazy { stringArrayRes(R.array.layers)[presenter.userLayer - 9] }

    private var hasInitTable = false
    private lateinit var rows: Array<TableRow>

    override fun init() {
        screenManager.setToolbarElevation(false)
    }

    override fun showData(changes: List<Change>) {
        fillTable(changes)
    }

    fun fillTable(changes: List<Change>) {
        if (!hasInitTable) {
            val standardColumnWidth = screenSize.x / 6
            val primaryColor = activity.primaryColor
            val rows: Array<TableRow?> = arrayOfNulls(11)

            repeat(presenter.classesAtLayer) { clazz ->
                repeat(MaxChangeHours + 1) { hour ->
                    if (rows[hour] == null) {
                        rows[hour] = TableRow(context).apply {
                            id = hour
                            layoutParams = TableLayout.LayoutParams().apply {
                                weight = 1.0f
                                height = 0
                                width = MATCH_PARENT
                            }
                        }

                        layerChangesTable.addView(rows[hour])
                    }

                    val cell = TextView(context).apply {
                        gravity = Gravity.CENTER
                        backgroundDrawable = drawableRes(R.drawable.cell_shape)
                        textColor = Color.WHITE
                        padding = 10

                        layoutParams = TableRow.LayoutParams(clazz).apply {
                            height = MATCH_PARENT
                            width = standardColumnWidth
                        }
                    }

                    if (hour == TitleRow) {
                        cell.setBackgroundColor(primaryColor)
                        cell.text = layerText + "'" + (clazz + 1).toString()
                        cell.setTypeface(null, Typeface.BOLD)
                    }

                    rows[hour]!!.addView(cell)
                }
            }
            this.rows = rows.requireNoNulls()
            hasInitTable = true
        }

        changes.forEach {
            (rows[it.hour].getChildAt(it.clazz - 1) as TextView).apply {
                backgroundColor = it.color
                text = it.content
            }
        }
    }

    companion object {
        private const val TitleRow = 0
        private const val MaxChangeHours = 10
    }
}