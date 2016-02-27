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

package com.ohelshem.app.android.timetable.adapter

import android.content.Context
import android.graphics.RectF
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.yoavst.changesystemohelshem.R
import com.ohelshem.app.controller.TimetableController
import com.ohelshem.api.model.Hour
import org.jetbrains.anko.*
import kotlinx.android.synthetic.main.timetable_item.view.*

/**
 * Showing timetable inside a [RecyclerView]
 */
class TimetableAdapter(val context: Context, val items: Array<Hour>, val listener: (Hour, Int) -> Unit) : RecyclerView.Adapter<TimetableAdapter.VH>() {
    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH = VH(context, parent)

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.lesson.text = (position + 1).toString()
        holder.text.text = item.name + " " + item.teacher
        holder.background.background = createLessonDrawable(item.color)
        holder.hour.text = TimetableController.getStartOfHour(position + 1)
        holder.tillHour.text = TimetableController.getEndOfHour(position + 1)
        holder.background.setOnClickListener {
            listener(item, position)
        }
    }


    class VH(context: Context, container: ViewGroup) : RecyclerView.ViewHolder(context.layoutInflater.inflate(R.layout.timetable_item, container, false)) {
        val text: TextView by lazy { itemView.text }
        val hour: TextView by lazy { itemView.hour }
        val tillHour: TextView by lazy { itemView.tillHour }
        val lesson: TextView by lazy { itemView.lesson }
        val background: View by lazy { itemView.find<View>(R.id.background) }
    }

    companion object {
        fun createLessonDrawable(color: Int): ShapeDrawable {
            val drawable = ShapeDrawable()
            val radius = 10
            val radii = floatArrayOf(radius.toFloat(), radius.toFloat(), radius.toFloat(), radius.toFloat(), radius.toFloat(), radius.toFloat(), radius.toFloat(), radius.toFloat())
            val shape = RoundRectShape(radii, RectF(), radii)
            drawable.shape = shape
            val paint = drawable.paint
            paint.color = color
            return drawable
        }
    }
}