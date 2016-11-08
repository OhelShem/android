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

package com.ohelshem.app.android.dates.list

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.github.salomonbrys.kodein.erased.instance
import com.ohelshem.api.model.Test
import com.ohelshem.app.android.dates.DatesPresenter
import com.ohelshem.app.android.dates.DatesView
import com.ohelshem.app.android.utils.BaseMvpFragment
import com.ohelshem.app.android.utils.adapter.HeaderAdapter
import com.ohelshem.app.clearTime
import com.ohelshem.app.model.VisibleItem
import com.yoavst.changesystemohelshem.R
import java.util.*

class DatesListFragment : BaseMvpFragment<DatesView, DatesPresenter>(), DatesView {
    override val layoutId: Int = R.layout.list
    override fun createPresenter(): DatesPresenter = with(kodein()) { DatesPresenter(instance()) }

    private val oldTests by lazy { getString(R.string.old_test) }

    lateinit var list: RecyclerView

    override fun init() {
        list = view as RecyclerView

        list.layoutManager = LinearLayoutManager(activity)
        list.setHasFixedSize(true)
    }

    override fun update(tests: List<Test>) {
        val time = Calendar.getInstance().clearTime().apply { add(Calendar.DAY_OF_YEAR, 7) }.timeInMillis
        val now = Calendar.getInstance().clearTime().timeInMillis
        val indexOfNew = tests.indexOfFirst { it.date > now }
        val items = if (indexOfNew == -1 || indexOfNew == tests.lastIndex) tests.map { VisibleItem(it) }
        else listOf(VisibleItem<Test>(oldTests)) +
                tests.take(indexOfNew).map { VisibleItem(it) } +
                HeaderAdapter.split(tests.drop(indexOfNew), getString(R.string.close_week), getString(R.string.later)) { date >= time }
        list.adapter = TestsAdapter(activity, items) {}
        if (indexOfNew > -1)
            list.scrollToPosition(indexOfNew + 1)
    }
}