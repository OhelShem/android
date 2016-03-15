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

package com.ohelshem.app.android.changes

import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import com.ohelshem.api.model.Change
import com.ohelshem.api.model.Hour
import com.ohelshem.app.android.changes.adapter.LayerChangesAdapter
import com.ohelshem.app.android.util.drawableRes
import com.ohelshem.app.android.util.view.DividerItemDecoration
import com.yoavst.changesystemohelshem.R
import kotlinx.android.synthetic.main.layer_changes_fragment.*

class LayerChangesFragment: BaseChangesFragment<LayerChangesPresenter>() {
    override val layoutId: Int = R.layout.layer_changes_fragment

    override fun createPresenter(): LayerChangesPresenter? = LayerChangesPresenter()

    override fun init() {
        recyclerView.layoutManager = GridLayoutManager(activity, 11, LinearLayoutManager.HORIZONTAL, true)
        recyclerView.addItemDecoration(DividerItemDecoration(activity.drawableRes(R.drawable.abc_list_divider_mtrl_alpha)))
        super.init()
    }

    override fun showData(changes: List<Change>) {
        recyclerView.adapter = LayerChangesAdapter(activity, changes, presenter.classesAtLayer)
    }
}