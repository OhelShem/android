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

package com.ohelshem.app.android.changes.adapter

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.ohelshem.api.model.Change
import com.yoavst.changesystemohelshem.R

class LayerChangesAdapter(val context: Context, val height: Int, items: List<Change>, val numberOfClasses: Int) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val NoChangesColor = arrayOf(Color.parseColor("#D9D9D9"), Color.parseColor("#BABABA"))
    private val items = Array(numberOfClasses) { clazz -> Array(10) { hour ->items.firstOrNull { it.clazz == clazz + 1 && it.hour == hour + 1 }}}
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val column = position / numberOfRows
        if (holder is TitleVH) {
            holder.textView.text = "" + (column + 1)
        } else if (holder is ItemVH) {
            holder.textView.isEnabled = true
            val row = position - (column * numberOfRows) - 1
            try {
                val change = items[column][row]
                holder.itemView.setBackgroundColor(if (change == null) NoChangesColor[row % 2] else change.color)
                holder.textView.text = change?.content?.trim() ?: ""
            } catch (e: IndexOutOfBoundsException) {
                holder.itemView.setBackgroundColor(Color.WHITE)
                holder.textView.text = ""
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        return if (viewType == typeItem) {
            val itemView = LayoutInflater.from(context).inflate(R.layout.layer_change_item, parent, false) as ViewGroup
            itemView.minimumHeight = height
            ItemVH(itemView)
        }
        else TitleVH(LayoutInflater.from(context).inflate(R.layout.layer_change_title, parent, false) as ViewGroup)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position % numberOfRows == 0) typeTitle
        else typeItem
    }

    override fun getItemCount(): Int = numberOfRows * numberOfClasses


    class ItemVH(view: ViewGroup) : RecyclerView.ViewHolder(view) {
        val textView: TextView by lazy { view.getChildAt(0) as TextView }
    }

    class TitleVH(view: ViewGroup) : RecyclerView.ViewHolder(view) {
        val textView: TextView by lazy { view.getChildAt(0) as TextView }
    }

    companion object {
        val numberOfRows = 11
        val typeTitle = 3
        val typeItem = 4
    }
}