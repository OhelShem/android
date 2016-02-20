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

import com.yoavst.changesystemohelshem.R
import com.ohelshem.app.android.adapter.TestsFragmentAdapter
import com.ohelshem.app.android.drawableRes
import com.ohelshem.app.model.ApiUpdatable
import com.ohelshem.api.controller.declaration.ApiController
import com.ohelshem.api.model.Test
import com.ohelshem.api.model.UpdateError
import kotlinx.android.synthetic.main.tests_fragment.pager
import kotlinx.android.synthetic.main.tests_fragment.tabs

/**
 * The fragment that is responsible on showing the tests.
 */
class TestsPagerFragment : BaseFragment(), ApiUpdatable<List<Test>> {
    override val layoutId: Int = R.layout.tests_fragment

    override val api: ApiController.Api = ApiController.Api.Tests

    override fun onUpdate() {
       pager.adapter = null
        pager.adapter = TestsFragmentAdapter(childFragmentManager)
    }

    override fun onUpdate(data: List<Test>) {
        onUpdate()
    }

    override fun onError(error: UpdateError) {
        // Ignored
    }

    override fun init() {
        onUpdate()
        drawerActivity.setToolbarTitle(getString(R.string.tests))
        tabs.setupWithViewPager(pager)
        tabs.getTabAt(0)!!.icon = activity.drawableRes(R.drawable.ic_list)
        tabs.getTabAt(1)!!.icon = activity.drawableRes(R.drawable.ic_calendar)
    }
}