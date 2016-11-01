/*
 * Copyright 2016 Yoav Sternberg.
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

package com.ohelshem.app.android.changes.layer

import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import com.github.salomonbrys.kodein.instance
import com.ohelshem.api.model.Change
import com.ohelshem.app.android.changes.BaseChangesFragment
import com.ohelshem.app.android.drawableRes
import com.ohelshem.app.android.stringArrayRes
import com.yoavst.changesystemohelshem.R
import kotlinx.android.synthetic.main.list_fragment.*
import org.jetbrains.anko.padding

class LayerChangesFragment : BaseChangesFragment<LayerChangesPresenter>() {
    override val layoutId: Int = R.layout.layer_changes_fragment

    override fun createPresenter(): LayerChangesPresenter = with(kodein()) { LayerChangesPresenter(instance(), instance(), instance()) }

    override fun init() {
        recyclerView.padding = 0
        recyclerView.layoutManager = GridLayoutManager(activity, MaxChangesHours + 1, LinearLayoutManager.HORIZONTAL, true)
        recyclerView.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.HORIZONTAL).apply { setDrawable(drawableRes(R.drawable.divider_white)!!) })
        recyclerView.setHasFixedSize(true)
    }

    override fun showData(changes: List<Change>) {
        recyclerView.adapter = LayerChangesAdapter(context, changes, presenter.classesAtLayer, MaxChangesHours, stringArrayRes(R.array.layers)[presenter.userLayer - 9])
        recyclerView.scrollToPosition(0)
    }

    companion object {
        private const val MaxChangesHours = 11
    }
}