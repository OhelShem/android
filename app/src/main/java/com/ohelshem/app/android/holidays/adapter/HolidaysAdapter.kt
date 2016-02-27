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

package com.ohelshem.app.android.holidays.adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.yoavst.changesystemohelshem.R
import com.ohelshem.app.android.util.hide
import com.ohelshem.app.android.util.show
import com.ohelshem.app.controller.TimetableController
import org.jetbrains.anko.layoutInflater
import kotlinx.android.synthetic.main.holiday_item.view.*


class HolidaysAdapter : RecyclerView.Adapter<HolidaysAdapter.HolidayHolder>() {
    override fun getItemCount(): Int = TimetableController.Holidays.size
    private val items = TimetableController.Holidays

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolidayHolder = HolidayHolder(parent.context.layoutInflater.inflate(R.layout.holiday_item, parent, false))

    override fun onBindViewHolder(holder: HolidayHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.name
        if (item.isOneDay()) {
            holder.endBlock.hide()
            holder.daysBlock.hide()
            holder.startText.hide()
        } else {
            holder.endBlock.show()
            holder.daysBlock.show()
            holder.startText.show()
            holder.endDate.text = item.end.substring(0,5)
            holder.length.text = item.length.toString()
        }
        holder.startDate.text = item.start.substring(0,5)
    }

    class HolidayHolder(layout: View) : RecyclerView.ViewHolder(layout) {
        val title: TextView by lazy { itemView.title }
        val startDate: TextView by lazy { itemView.startDate }
        val endDate: TextView by lazy { itemView.endDate }
        val length: TextView by lazy { itemView.length }
        val endBlock: View by lazy { itemView.endBlock }
        val daysBlock: View by lazy { itemView.daysBlock }
        val startText: TextView by lazy { itemView.startText }

    }
}