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

package com.ohelshem.app.android.settings.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.View
import android.widget.TextView
import com.yoavst.changesystemohelshem.R
import com.ohelshem.app.model.OverrideData
import com.ohelshem.app.model.VisibleItem
import com.ohelshem.api.model.Hour
import com.ohelshem.app.android.util.adapter.SimpleHeaderAdapter
import org.jetbrains.anko.*

class OverridesAdapter(context: Context, items: MutableList<VisibleItem<Pair<OverrideData, Hour>>>, callback: (Pair<OverrideData, Hour>) -> Unit)
: SimpleHeaderAdapter<Pair<OverrideData, Hour>>(context, items, callback) {
    override val layout: Int = R.layout.item_override

    override fun onCreateViewHolder(layout: View): ViewHolder = OverridesHolder(layout)

    override fun onBindViewHolder(holder: ViewHolder, item: Pair<OverrideData, Hour>) {
        holder as OverridesHolder
        holder.original.text = item.second.name
        holder.current.text = item.first.newName
        holder.originalTeacher.text = item.second.teacher
        holder.currentTeacher.text = item.first.newTeacher
        holder.hour.text = (item.first.hour + 1).toString()
    }

    private class OverridesHolder(layout: View) : ViewHolder(layout) {
        val original: TextView by lazy { itemView.find<TextView>(R.id.original) }
        val current: TextView by lazy { itemView.find<TextView>(R.id.current) }
        val hour: TextView by lazy { itemView.find<TextView>(R.id.hour) }
        val originalTeacher: TextView by lazy { itemView.find<TextView>(R.id.originalTeacher) }
        val currentTeacher: TextView by lazy { itemView.find<TextView>(R.id.currentTeacher) }
    }
}