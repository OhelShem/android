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

package com.ohelshem.app.android.holidays.list

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ohelshem.app.android.hide
import com.ohelshem.app.android.show
import com.ohelshem.app.controller.timetable.TimetableController.Companion.Holiday
import com.yoavst.changesystemohelshem.R
import kotlinx.android.synthetic.main.holiday_item.view.*
import org.jetbrains.anko.layoutInflater

class HolidaysAdapter(val holidays: Array<Holiday>) : RecyclerView.Adapter<HolidaysAdapter.HolidayHolder>() {
    override fun getItemCount(): Int = holidays.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolidayHolder = HolidayHolder(parent.context.layoutInflater.inflate(R.layout.holiday_item, parent, false))

    override fun onBindViewHolder(holder: HolidayHolder, position: Int) {
        val item = holidays[position]
        holder.title.text = item.name
        if (holder.extra != null) {
            if (item.isOneDay()) {
                holder.extra.endBlock.hide()
                holder.extra.daysBlock.hide()
                holder.extra.startText.hide()
            } else {
                holder.extra.endBlock.show()
                holder.extra.daysBlock.show()
                holder.extra.startText.show()
                holder.extra.endDate.text = item.end.substring(0, 5)
                holder.extra.length.text = item.length.toString()
            }
            holder.startDate.text = item.start.substring(0, 5)
        } else {
            if (item.isOneDay()) {
                holder.startDate.text = item.start.substring(0, 5)
            } else {
                holder.startDate.text = item.start.substring(0, 5) + " - " + item.end.substring(0, 5)

            }
        }
    }

    class HolidayHolder(layout: View) : RecyclerView.ViewHolder(layout) {
        val title: TextView = itemView.title
        val startDate: TextView = itemView.startDate
        val extra: ExtraHolder? = if (itemView.endDate != null) ExtraHolder(layout) else null
    }

    class ExtraHolder(layout: View) {
        val endDate: TextView = layout.endDate
        val length: TextView = layout.length
        val endBlock: View = layout.endBlock
        val daysBlock: View = layout.daysBlock
        val startText: TextView = layout.startText
    }
}