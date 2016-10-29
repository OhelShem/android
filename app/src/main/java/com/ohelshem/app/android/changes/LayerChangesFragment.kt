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
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import com.github.salomonbrys.kodein.instance
import com.ohelshem.api.model.Change
import com.ohelshem.app.android.get
import com.yoavst.changesystemohelshem.R
import kotlinx.android.synthetic.main.layer_changes_fragment.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.childrenSequence

class LayerChangesFragment : BaseChangesFragment<LayerChangesPresenter>() {
    override val layoutId: Int = R.layout.layer_changes_fragment

    override fun createPresenter(): LayerChangesPresenter = with(kodein()) { LayerChangesPresenter(instance(), instance(), instance()) }

    private var hasInitTable = false
    private lateinit var rows: List<LinearLayout>

    override fun init() = Unit

    override fun showData(changes: List<Change>) {
        //FIXME
        fillTable(changes)
    }

    private fun initLayout() {
        val view = LayerChangesGenerator.createView(activity, presenter.classesAtLayer, presenter.userLayer)
        tableScrollView.addView(view)
        this.rows = view.childrenSequence().drop(1).map { it as LinearLayout }.toList()
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
    }
}