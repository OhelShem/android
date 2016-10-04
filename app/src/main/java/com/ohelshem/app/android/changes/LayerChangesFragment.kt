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

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.Gravity
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import com.github.salomonbrys.kodein.instance
import com.ohelshem.api.model.Change
import com.ohelshem.app.android.drawableRes
import com.ohelshem.app.android.utils.view.DividerItemDecoration
import com.yoavst.changesystemohelshem.R
import kotlinx.android.synthetic.main.layer_changes_fragment.*
import org.jetbrains.anko.textColor

class LayerChangesFragment : BaseChangesFragment<LayerChangesPresenter>() {
    override val layoutId: Int = R.layout.layer_changes_fragment

    override fun createPresenter(): LayerChangesPresenter = with(kodein()) { LayerChangesPresenter(instance(), instance(), instance()) }

    override fun init() {
        screenManager.setToolbarElevation(false)

        recyclerView.layoutManager = GridLayoutManager(activity, 11, LinearLayoutManager.HORIZONTAL, true)
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(DividerItemDecoration(activity.drawableRes(R.drawable.abc_list_divider_mtrl_alpha)))
    }

    override fun showData(changes: List<Change>) {
        fillTable(changes)
        //recyclerView.adapter = LayerChangesAdapter(activity, recyclerView.measuredHeight / 11, changes, presenter.classesAtLayer)
    }

    fun fillTable(changes: List<Change>) {

        val screen = activity.getWindowManager().getDefaultDisplay()
        val size = Point()
        screen.getSize(size)

        val standardColumnWidth = size.x / 6 //every column takes 1/6 of the screen horizontally

        val rows = kotlin.arrayOfNulls<TableRow>(11)
        val cells = arrayOfNulls<TextView>(presenter.classesAtLayer*10)

        for (c in 0..(presenter.classesAtLayer-1)) {
            for (i in 0..10) { //added a row for the top title
                if (rows[i] == null) { //if current row is null, initialize it
                    rows[i] = TableRow(activity)
                    val rowParams = TableLayout.LayoutParams()
                    rowParams.weight = 1.0f
                    rowParams.height = 0
                    rowParams.width = TableLayout.LayoutParams.MATCH_PARENT
                    rows[i]?.setLayoutParams(rowParams)
                    rows[i]?.setId(i)
                    //add the initialized row to the layout
                    layerChangesTable.addView(rows[i])
                }


                cells[i] = TextView(activity)
                cells[i]?.setGravity(Gravity.CENTER)

                val sdk = android.os.Build.VERSION.SDK_INT
                if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        cells[i]?.setBackgroundDrawable(ContextCompat.getDrawable(activity, R.drawable.cell_shape))
                    } else {
                        cells[i]?.setBackground(ContextCompat.getDrawable(activity, R.drawable.cell_shape))
                    }

                cells[i]?.textColor = Color.parseColor("#FFFFFF")

                cells[i]?.setPadding(10, 10, 10, 10)

                //add any matching change to the current cell
                changes.forEach {
                    if (it.clazz==(c+1) && it.hour==(i-1)) {
                        cells[i]?.setBackgroundColor(it.color)
                        cells[i]?.setText(it.content)
                    }
                }

                if (i==0) { //if current row is the title row
                    cells[i]?.setBackgroundColor(Color.parseColor("#5677fc"))
                    cells[i]?.setText(layerText + "'" +(c+1).toString())
                    cells[i]?.setTypeface(null, Typeface.BOLD)
                }

                val cellParams = TableRow.LayoutParams(c)

                cellParams.height = TableRow.LayoutParams.MATCH_PARENT
                // cellParams.setMargins(5, 5, 5, 5);
                cellParams.width = standardColumnWidth

                cells[i]?.setLayoutParams(cellParams)

                //add the cell to the row
                rows[i]?.addView(cells[i])

            }
        }
    }

    fun Context.stringArrayRes(id: Int): Array<String> = resources.getStringArray(id)
    private val layer: Int by lazy { presenter.userLayer }
    private val layerText: String by lazy { activity.stringArrayRes(R.array.layers)[layer - 9] }

}