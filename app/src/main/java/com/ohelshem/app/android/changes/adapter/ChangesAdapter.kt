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

package com.ohelshem.app.android.changes.adapter

import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.ohelshem.api.model.Change
import com.ohelshem.api.model.Hour
import com.ohelshem.app.android.timetable.adapter.TimetableAdapter
import com.ohelshem.app.controller.timetable.TimetableController
import com.yoavst.changesystemohelshem.R
import org.jetbrains.anko.backgroundDrawable
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.onClick

/**
 * Showing changes inside a [RecyclerView]. Based on [TimetableAdapter] UI.
 */
class ChangesAdapter(items: List<Change>, val timetable: Array<Hour>, val coordinatorLayout: CoordinatorLayout) : RecyclerView.Adapter<TimetableAdapter.VH>() {
    val EmptyCallback = { v: View? -> }
    val items = Array(Math.max(items.maxBy { it.hour }!!.hour, timetable.size)) { position -> items.firstOrNull { it.hour - 1 == position } }
    val with = " " + coordinatorLayout.context.getString(R.string.with) + " "

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimetableAdapter.VH = TimetableAdapter.VH(parent.context, parent)

    override fun onBindViewHolder(holder: TimetableAdapter.VH, position: Int) {
        val item = items[position]
        val hour = if (position < timetable.size) timetable[position] else null
        holder.lesson.text = (position + 1).toString()
        if (item == null) {
            holder.text.text = hour?.let { it.name + " " + it.teacher } ?: ""
            holder.background.backgroundDrawable = TimetableAdapter.createLessonDrawable(hour?.color ?: TimetableController.ColorEmpty)
            holder.lesson.backgroundResource = R.drawable.number_circle
            holder.itemView.onClick(EmptyCallback)
        } else {
            holder.text.text = item.content
            holder.background.background = TimetableAdapter.createLessonDrawable(item.color)
            holder.lesson.backgroundResource = R.drawable.number_circle_change
            holder.background.onClick { v ->
                if (!(hour?.isEmpty() ?: true))
                    snackbar(hour!!.name + with + hour.teacher)
                else
                    snackbar(R.string.floating_hour)
            }
        }
        holder.hour.text = TimetableController.getStartOfHour(position + 1)
        holder.tillHour.text = TimetableController.getEndOfHour(position + 1)
    }

    private fun snackbar(text: String) {
        Snackbar.make(coordinatorLayout, text, Snackbar.LENGTH_SHORT).show()
    }

    private fun snackbar(text: Int) {
        Snackbar.make(coordinatorLayout, text, Snackbar.LENGTH_SHORT).show()
    }
}

