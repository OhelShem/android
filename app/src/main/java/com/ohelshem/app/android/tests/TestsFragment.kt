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

package com.ohelshem.app.android.tests

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.Menu
import android.view.MenuInflater
import com.hannesdorfmann.mosby.mvp.MvpFragment
import com.ohelshem.api.model.Test
import com.ohelshem.app.android.base.fragment.BaseMvpFragment
import com.ohelshem.app.android.tests.adapter.TestsFragmentAdapter
import com.ohelshem.app.android.util.drawableRes
import com.yoavst.changesystemohelshem.R
import kotlinx.android.synthetic.main.tests_fragment.*
import org.jetbrains.anko.support.v4.toast

class TestsFragment : BaseMvpFragment<TestsView, TestsPresenter>(), TestsView {
    override val layoutId: Int = R.layout.tests_fragment
    override fun createPresenter(): TestsPresenter = TestsPresenter()

    override fun init() {
        screenManager.screenTitle = getString(R.string.tests)
        pager.adapter = TestsFragmentAdapter(childFragmentManager)
        tabs.setupWithViewPager(pager)
        tabs.getTabAt(0)!!.icon = activity.drawableRes(R.drawable.ic_list)
        tabs.getTabAt(1)!!.icon = activity.drawableRes(R.drawable.ic_calendar)
        presenter.init()
    }

    override fun onFragmentLoaded() {
        presenter.load()
    }

    override fun update(tests: List<Test>) {
        childFragmentManager.fragments?.forEach {
            @Suppress("UNCHECKED_CAST")
            (it as? MvpFragment<*, TestsChildPresenter>)?.getPresenter()?.update(tests)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.menu_mashov).setOnMenuItemClickListener {
            val intent = activity.packageManager.getLaunchIntentForPackage("com.yoavst.mashov")
            if (isGraderInstalled() && intent!=null) {
                intent.addCategory(Intent.CATEGORY_LAUNCHER)
                activity.startActivity(intent)
            } else {
                toast(getString(R.string.menu_mashov_message))
                launchPlayStore("com.yoavst.mashov")
            }

            true
        }
    }

    private fun isGraderInstalled(): Boolean {
        try {
            context.packageManager.getApplicationInfo("com.yoavst.mashov", 0)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
            return false
        }

    }

    private fun launchPlayStore(packageName: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)))
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + packageName)))
        }
    }

    override var menuId: Int = R.menu.tests

}