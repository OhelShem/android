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

package com.ohelshem.app.android.fragment

import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.vlonjatg.progressactivity.ProgressActivity
import com.yoavst.changesystemohelshem.R
import com.ohelshem.app.android.adapter.LayerChangesAdapter
import com.ohelshem.app.android.drawableRes
import com.ohelshem.app.android.view.DividerItemDecoration
import com.ohelshem.app.controller.DBController
import com.ohelshem.api.model.Change
import com.ohelshem.api.model.UpdateError
import kotlinx.android.synthetic.main.layer_changes_fragment.*

class LayerChangesFragment : BaseChangesFragment() {
    override val layoutId: Int = R.layout.layer_changes_fragment
    override val emptyLayout: ProgressActivity by lazy { progressActivity }
    override val recyclerView: RecyclerView by lazy { list }

    override fun init() {
        recyclerView.layoutManager = GridLayoutManager(activity, 11, LinearLayoutManager.HORIZONTAL, true)
        recyclerView.addItemDecoration(DividerItemDecoration(activity.drawableRes(R.drawable.abc_list_divider_mtrl_alpha)))
        if (databaseController.isCacheUpdated()) {
            iffData()
        } else noData()
    }

    override fun onUpdate(data: List<Change>) {
        if (databaseController.isCacheUpdated()) {
            emptyLayout.showContent()
            val changes = databaseController.changes
            if (changes != null)
                recyclerView.adapter = LayerChangesAdapter(activity, data, DBController.Companion.classesAtLayer(databaseController.userData.layer))
            else noData()
            setTitle()
        } else onError(UpdateError.NoData)
    }
}