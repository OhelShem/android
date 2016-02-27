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

package com.ohelshem.app.android.util.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.ohelshem.app.model.VisibleItem
import java.util.*

abstract class HeaderAdapter<K : Any>(context: Context, val items: MutableList<VisibleItem<K>>, private val callback: (K) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == TypeHeader) onBindHeaderViewHolder(holder, items[position].title!!)
        else {
            holder.itemView.setOnClickListener { callback(items[position].data!!) }
            onBindViewHolder(holder, items[position].data!!)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TypeHeader) return onCreateHeaderViewHolder(inflater, parent)
        else return onCreateViewHolder(inflater, parent, viewType)
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position].title == null) TypeData else TypeHeader
    }

    override fun getItemCount(): Int = items.size


    protected abstract fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: K)
    protected abstract fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
    protected abstract fun onCreateHeaderViewHolder(inflater: LayoutInflater, parent: ViewGroup): RecyclerView.ViewHolder
    protected abstract fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder, title: String)


    companion object {
        private val TypeHeader: Int = 1
        private val TypeData: Int = 0

        fun <K : Any> split(data: List<K>, firstTitle: String, secondTitle: String, rule: K.() -> Boolean): MutableList<VisibleItem<K>> {
            var oldPosition = -1
            for (i in 0..data.size - 1) {
                if (data[i].rule()) {
                    oldPosition = i
                    break
                }
            }
            if (oldPosition == -1 || oldPosition == 0) {
                val list = ArrayList<VisibleItem<K>>(data.size)
                for (i in 0 until data.size)
                    list += VisibleItem(data[i])
                return list
            } else {
                val list = ArrayList<VisibleItem<K>>(data.size + 2)
                for (i in 0 until data.size) {
                    list +=
                            if (i == 0) VisibleItem<K>(title = firstTitle)
                            else if (i == oldPosition + 1) VisibleItem<K>(title = secondTitle)
                            else if (i < oldPosition + 1) VisibleItem(data[i - 1])
                            else VisibleItem(data[i - 2])
                }
                return list
            }
        }
    }
}
