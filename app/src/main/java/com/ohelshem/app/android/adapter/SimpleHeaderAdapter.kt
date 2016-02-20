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

package com.ohelshem.app.android.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.yoavst.changesystemohelshem.R
import com.ohelshem.app.model.VisibleItem

abstract class SimpleHeaderAdapter<K : Any>(context: Context, items: MutableList<VisibleItem<K>>, callback: (K) -> Unit) : HeaderAdapter<K>(context, items, callback) {
    protected abstract val layout: Int

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView as TextView
    }

    override fun onCreateHeaderViewHolder(inflater: LayoutInflater, parent: ViewGroup): RecyclerView.ViewHolder {
        return HeaderViewHolder(inflater.inflate(R.layout.item_header, parent, false))
    }

    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder, title: String) {
        (holder as HeaderViewHolder).title.text = title
    }

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return onCreateViewHolder(inflater.inflate(layout, parent, false))
    }

    abstract fun onCreateViewHolder(layout: View): RecyclerView.ViewHolder

}