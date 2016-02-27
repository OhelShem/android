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

package com.ohelshem.app.android.tests.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.yoavst.changesystemohelshem.R
import com.ohelshem.app.android.util.adapter.HeaderAdapter
import com.ohelshem.app.android.tests.adapter.TestsAdapter
import com.ohelshem.app.android.util.fragment.BaseFragment
import com.ohelshem.app.clearTime
import com.ohelshem.app.controller.DBController
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.dip
import uy.kohesive.injekt.injectLazy
import java.util.*

/**
 * Fragment for showing tests as list
 */
class TestsFragment : BaseFragment() {
    private val databaseController: DBController by injectLazy()
    private lateinit var recyclerView: RecyclerView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        recyclerView = RecyclerView(activity)
        recyclerView.padding = dip(8)
        recyclerView.clipToPadding = false
        return recyclerView
    }

    override fun init() {
        recyclerView.layoutManager = LinearLayoutManager(activity)
        val time = Calendar.getInstance().clearTime().apply { add(Calendar.DAY_OF_YEAR, 7) }.timeInMillis
        val items = HeaderAdapter.split(databaseController.tests!!, getString(R.string.close_week), getString(R.string.later)) { date <= time }
        recyclerView.adapter = TestsAdapter(activity, items) {

        }
    }
}