/*
 * Copyright 2010-2015 Yoav Sternberg.
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

package com.ohelshem.app.android.dates.list

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ohelshem.api.model.Test
import com.ohelshem.app.android.utils.adapter.SimpleHeaderAdapter
import com.ohelshem.app.model.VisibleItem
import com.yoavst.changesystemohelshem.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * An adapter for showing tests
 */
class TestsAdapter(context: Context, items: List<VisibleItem<Test>>, callback: (Test) -> Unit) : SimpleHeaderAdapter<Test>(context, items.toMutableList(), callback) {
    override val layout: Int = R.layout.item_1_line
    private val now = Date().time

    override fun onCreateViewHolder(layout: View): TestsHolder = TestsHolder(layout)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: Test) {
        holder as TestsHolder
        holder.title.text = item.content
        holder.date.text = DateFormat.format(Date(item.date))
        if (now > item.date)
            holder.indicator.text = "✓"
        else
            holder.indicator.text = ""

    }

    class TestsHolder(layout: View) : RecyclerView.ViewHolder(layout) {
        val indicator: TextView by lazy { (layout as ViewGroup).getChildAt(2) as TextView }
        val title: TextView by lazy { (layout as ViewGroup).getChildAt(1) as TextView }
        val date: TextView by lazy { (layout as ViewGroup).getChildAt(0) as TextView }
    }

    companion object {
        private val DateFormat = SimpleDateFormat("dd/MM/yy")
    }

}