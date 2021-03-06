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

package com.ohelshem.app.android.timetable.adapter

import android.content.Context
import android.content.res.Resources
import android.support.v7.widget.ThemedSpinnerAdapter
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.yoavst.changesystemohelshem.R
import org.jetbrains.anko.find

/**
 * An adapter for the day spinner for the toolbar.
 */
class DaySpinnerAdapter(context: Context, daysLearning: BooleanArray) : BaseAdapter(), ThemedSpinnerAdapter {
    private val mDropDownHelper: ThemedSpinnerAdapter.Helper = ThemedSpinnerAdapter.Helper(context)
    private val daysOfWeek = context.resources.getStringArray(R.array.week_days)
    private val allWeek = context.getString(R.string.all_week)
    private val days: IntArray = IntArray(daysLearning.count { it } + 1)

    init {
        var pos = 0
        daysLearning.forEachIndexed { index, enabled ->
            if (enabled)
                days[++pos] = index + 1
        }
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = if (convertView == null) {
            // Inflate the drop down using the helper's LayoutInflater
            val inflater = mDropDownHelper.dropDownViewInflater
            inflater.inflate(R.layout.support_simple_spinner_dropdown_item, parent, false)
        } else convertView

        view.find<TextView>(android.R.id.text1).apply {
            text = if (position == 0) allWeek else daysOfWeek[getItem(position) - 1]
            gravity = Gravity.CENTER
        }
        return view
    }

    override fun setDropDownViewTheme(theme: Resources.Theme?) {
        mDropDownHelper.dropDownViewTheme = theme
    }

    override fun getDropDownViewTheme(): Resources.Theme? = mDropDownHelper.dropDownViewTheme

    override fun getCount(): Int = days.size

    override fun getItem(position: Int) = days[position]

    override fun getItemId(position: Int): Long = 0

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val view: View
        val holder: ViewHolder
        if (convertView != null) {
            view = convertView
            holder = view.tag as ViewHolder
        } else {
            view = LayoutInflater.from(parent.context).inflate(R.layout.support_simple_spinner_dropdown_item, parent, false)
            holder = ViewHolder(view.find(android.R.id.text1))
            view.tag = holder
        }

        holder.title.apply {
            text = if (position == 0) allWeek else daysOfWeek[position - 1]
            gravity = Gravity.CENTER
        }
        return view
    }

    class ViewHolder(val title: TextView)
}
