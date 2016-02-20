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

package com.ohelshem.app.android.adapter

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.yoavst.changesystemohelshem.R
import com.ohelshem.app.controller.TimetableControllerWrapper
import com.ohelshem.api.model.Change
import com.ohelshem.api.model.Hour
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.onClick


class GuessingChangesAdapter(originalItems: List<Change>, val timetable: Array<Hour>, val successCallback: () -> Unit, val finalCallback: () -> Unit) : RecyclerView.Adapter<TimetableAdapter.VH>() {
    val items: Array<Change?> = Array(Math.max(originalItems.maxBy { it.hour }!!.hour, timetable.size)) { position -> originalItems.firstOrNull { it.hour - 1 == position } }
    var changes = items.count { it != null }
    var stillHidden = this.items.size
    var showAll = false
    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimetableAdapter.VH = TimetableAdapter.VH(parent.context, parent)

    override fun onBindViewHolder(holder: TimetableAdapter.VH, position: Int) {
        val item = items[position]
        val hour = if (position < timetable.size) timetable[position] else null
        holder.lesson.text = (position + 1).toString()
        if (showAll || holder.itemView.tag != null) {
            if (item == null) {
                holder.text.text = hour?.let { it.name + " " + it.teacher } ?: ""
                holder.background.background = TimetableAdapter.createLessonDrawable(hour?.color ?: TimetableControllerWrapper.ColorEmpty)
                holder.lesson.backgroundResource = R.drawable.number_circle
            } else {
                holder.text.text = item.content
                holder.background.background = TimetableAdapter.createLessonDrawable(item.color)
                holder.lesson.backgroundResource = R.drawable.number_circle_change
            }
            holder.background.onClick { }
        } else {
            holder.text.text = "?"
            holder.background.background = TimetableAdapter.createLessonDrawable(TimetableControllerWrapper.ColorEmpty)
            holder.lesson.backgroundResource = R.drawable.number_circle
            holder.background.onClick {
                if (item != null) {
                    changes--
                    successCallback()
                    if (changes == 0) {
                        showAll = true
                        notifyDataSetChanged()
                        finalCallback()
                        return@onClick
                    }

                }
                holder.itemView.tag = Object()
                onBindViewHolder(holder, position)
                stillHidden--
                if (stillHidden == 0) finalCallback()
            }
        }


    }
}