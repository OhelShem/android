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

package com.ohelshem.app.android.utils.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.ohelshem.app.model.VisibleItem

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
        return if (viewType == TypeHeader) onCreateHeaderViewHolder(inflater, parent)
        else onCreateViewHolder(inflater, parent, viewType)
    }

    override fun getItemViewType(position: Int): Int = if (items[position].title == null) TypeData else TypeHeader

    override fun getItemCount(): Int = items.size


    protected abstract fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: K)
    protected abstract fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
    protected abstract fun onCreateHeaderViewHolder(inflater: LayoutInflater, parent: ViewGroup): RecyclerView.ViewHolder
    protected abstract fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder, title: String)


    companion object {
        private val TypeHeader: Int = 1
        private val TypeData: Int = 0

        fun <K : Any> split(data: List<K>, firstTitle: String, secondTitle: String, rule: K.() -> Boolean): MutableList<VisibleItem<K>> {
            val transform: List<K>.() -> MutableList<VisibleItem<K>> = { map { VisibleItem(it) }.toMutableList()}
            val before = data.takeWhile { !rule(it) }
            if (before.isEmpty()) return (listOf(VisibleItem<K>(secondTitle)) + data.transform()).toMutableList()
            return (sequenceOf(VisibleItem<K>(firstTitle)) + before.transform() + VisibleItem(secondTitle) + data.drop(before.size).transform()).toMutableList()
        }
    }
}
