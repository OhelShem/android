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

package com.ohelshem.app.android.activity

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.Spinner
import com.yoavst.changesystemohelshem.R
import com.ohelshem.app.android.colorRes
import com.ohelshem.app.android.drawableRes
import com.ohelshem.app.android.hide
import com.ohelshem.app.android.show
import com.ohelshem.app.controller.DBController
import com.ohelshem.app.model.ApiUpdatable
import com.ohelshem.app.model.DrawerActivity
import com.ohelshem.app.model.DrawerActivity.Companion.FragmentType
import com.ohelshem.app.controller.ApiController
import com.ohelshem.api.model.UpdateError
import com.ohelshem.app.android.fragment.*
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.main_activity.view.*
import org.jetbrains.anko.*
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import uy.kohesive.injekt.injectLazy
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), DrawerActivity, ApiController.Callback {
    private val databaseController: DBController by injectLazy()
    private val apiController: ApiController by injectLazy()

    private val userLayerText by lazy { resources.getStringArray(R.array.layers)[databaseController.userData.layer - 9] }
    private var lastUpdate: Long = 0

    private var queuedId: Int = -1
    private var fragmentStack: Stack<FragmentType> = Stack()
    private var miniDrawerItems: Array<ImageView>? = null
    private val selectedColor by lazy { colorRes(R.color.colorAccent) }
    private lateinit var updatedAt: String
    private lateinit var headerView: View

    //region Activity events
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!databaseController.isSetup()) {
            startActivity<LoginActivity>()
            overridePendingTransition(0, 0)
            finish()
        } else {
            setContentView(R.layout.main_activity)
            initDrawer()
            lastUpdate = databaseController.updateDate
            if (savedInstanceState == null) {
                if (resources.getBoolean(R.bool.dashboard_as_default))
                    setFragment(FragmentType.Dashboard)
                else setFragment(FragmentType.Timetable)
                refresh()
            } else setFragment(FragmentType.values()[savedInstanceState.getInt(Key_Fragment)])
            if (extraFragment != null)
                supportFragmentManager.beginTransaction().replace(R.id.extraFragment, DashboardFragment()).commit()
            /*+-if (secondaryExtraFragment != null)
                supportFragmentManager.beginTransaction().replace(R.id.secondaryExtraFragment, TestsFragment()).commit()*/
        }
    }

    override fun onResume() {
        super.onResume()
        apiController[CallbackId] = this
        if (lastUpdate != databaseController.updateDate) {
            lastUpdate = databaseController.updateDate
            updateFragment()
        }
    }

    override fun onPause() {
        super.onPause()
        apiController -= CallbackId
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(Key_Fragment, fragmentStack.peek().ordinal)
    }

    override fun onBackPressed() {
        if (fragmentStack.size > 1) {
            fragmentStack.pop() // current fragment
            setFragment(fragmentStack.peek())
        } else super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.main, menu)
        menu.findItem(R.id.refresh).setOnMenuItemClickListener {
            refresh()
            true
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 42 && resultCode == 42) {
            logout()
        }
    }
    //endregion

    //region Drawer
    private fun initDrawer() {
        toolbar.title = ""
        setSupportActionBar(toolbar)
        if (miniDrawerLayout != null) {
            headerView = header
            updatedAt = ""
            classText.text = userLayerText + "'" + databaseController.userData.clazz.toString()
            miniDrawerItems = arrayOf(changesDrawerItem.getChildAt(0) as ImageView, timetableDrawerItem.getChildAt(0) as ImageView,
                    layerChangesDrawerItem.getChildAt(0) as ImageView, holidaysDrawerItem.getChildAt(0) as ImageView)
            changesDrawerItem.onClick {
                setFragment(FragmentType.Changes)
            }
            timetableDrawerItem.onClick {
                setFragment(FragmentType.Timetable)
            }
            layerChangesDrawerItem.onClick {
                setFragment(FragmentType.LayerChanges)
            }
            regulationsDrawerItem.onClick {
                openRegulations()
            }
            helpDrawerItem.onClick {
                openHelp()
            }
            holidaysDrawerItem.onClick {
                setFragment(FragmentType.Holidays)
            }
            settingsDrawerItem.onClick {
                openSettings()
            }
        } else {
            if (Build.VERSION.SDK_INT >= 21) {
                // If there is drawer layout
                if (navigationView != null)
                    window.statusBarColor = Color.TRANSPARENT
            }
            updatedAt = getString(R.string.updated_at)
            navigationView.menu.findItem(R.id.layer).title = getString(R.string.layer) + " " + userLayerText
            headerView = navigationView.inflateHeaderView(R.layout.main_drawer_header).apply {
                classText.text = getString(R.string.clazz) + " " + userLayerText + "'" + databaseController.userData.clazz.toString()
            }
            navigationView.setNavigationItemSelectedListener {
                queuedId = it.itemId
                drawerLayout.closeDrawer(navigationView)
                true
            }
            toolbar.navigationIcon = drawableRes(R.drawable.ic_menu)
            toolbar.setNavigationOnClickListener {
                openDrawer()
            }
            drawerLayout.setDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
                override fun onDrawerClosed(drawerView: View?) {
                    if (queuedId != -1) {
                        when (queuedId) {
                            R.id.dashboard -> setFragment(FragmentType.Dashboard)
                            R.id.changes -> setFragment(FragmentType.Changes)
                            R.id.timetable -> setFragment(FragmentType.Timetable)
                            R.id.tests -> setFragment(FragmentType.Tests)
                            R.id.layer -> setFragment(FragmentType.LayerChanges)
                            R.id.holidays -> setFragment(FragmentType.Holidays)
                            R.id.help -> {
                                openHelp()
                            }
                            R.id.settings -> {
                                openSettings()
                            }
                            R.id.regulations -> {
                                openRegulations()
                            }
                            else -> throw IllegalArgumentException()
                        }
                        queuedId = -1
                    }
                }
            })
        }
        val lastUpdate = databaseController.updateDate
        if (lastUpdate != DBController.EmptyData.toLong())
            updateLastUpdated(lastUpdate)
    }

    private fun setSelected(type: FragmentType) {
        if (miniDrawerItems != null) {
            miniDrawerItems!!.forEach { it.setColorFilter(ColorUnselected) }
            if (type == FragmentType.Changes)
                miniDrawerItems!![0].setColorFilter(selectedColor)
            else if (type == FragmentType.Timetable)
                miniDrawerItems!![1].setColorFilter(selectedColor)
            else
                miniDrawerItems!![2].setColorFilter(selectedColor)
        } else navigationView.menu.getItem(type.ordinal).isChecked = true
    }

    private fun openSettings() {
        startActivityForResult<SettingsActivity>(42)
    }

    private fun openHelp() {
        startActivity<HelpActivity>()
    }

    private fun openRegulations() {
        if (!RegulationFile.exists())
            resources.openRawResource(R.raw.regulations).use { regulationStream ->
                RegulationFile.createNewFile()
                FileOutputStream(RegulationFile).use {
                    regulationStream.copyTo(it)
                }
            }
        val intent = Intent(Intent.ACTION_VIEW).setDataAndType(Uri.parse("content://$packageName/$RegulationFilename"), "application/pdf")
        if (packageManager.queryIntentActivities(intent, 0).isEmpty()) {
            toast("No PDF reader installed")
        } else startActivity(Intent.createChooser(intent, getString(R.string.choose_opener)))
    }

    private fun updateLastUpdated(time: Long) {
        headerView.lastUpdated.text = updatedAt + HourFormatter.format(Date(time))
    }

    override fun openDrawer() {
        drawerLayout.openDrawer(Gravity.LEFT)
    }
    //endregion

    fun logout() {
        Injekt.get<DBController>().clearData()
        startActivity(intentFor<LoginActivity>().clearTask())
        finish()
    }

    override fun setFragment(fragmentType: FragmentType, backStack: Boolean) {
        val fragment: Fragment = when (fragmentType) {
            FragmentType.Dashboard -> DashboardFragment()
            FragmentType.Changes -> ChangesFragment()
            FragmentType.Timetable -> TimetableFragment()
            FragmentType.Tests -> TestsPagerFragment()
            FragmentType.LayerChanges -> LayerChangesFragment()
            FragmentType.Holidays -> HolidaysFragment()
        }
        supportFragmentManager.beginTransaction().replace(R.id.fragment, fragment).commit()
        if (!backStack)
            fragmentStack.clear()
        fragmentStack.add(fragmentType)
        setSelected(fragmentType)
    }

    private fun getUpdatableFragment(): ApiUpdatable<*>? {
        @Suppress("UNCHECKED_CAST")
        return supportFragmentManager.findFragmentById(R.id.fragment) as? ApiUpdatable<*>
    }

    private fun updateFragment(apis: List<ApiController.Api>? = null) {
        val fragment = getUpdatableFragment()
        if (fragment != null) {
            if (apis == null || fragment.api == null || fragment.api!! in apis) {
                fragment.onUpdate()
            }
        }
    }

    private fun errorFragment(updateError: UpdateError) {
        getUpdatableFragment()?.onError(updateError)
    }

    override fun refresh() {
        if (apiController.update()) {
            toast(R.string.refreshing)
        }
    }

    override fun getNavigationSpinner(): Spinner {
        toolbar.title = ""
        supportActionBar?.setDisplayShowTitleEnabled(false)
        navigationSpinner.show()
        return navigationSpinner
    }

    override fun setToolbarTitle(title: String) {
        navigationSpinner.adapter = null
        navigationSpinner.hide()
        supportActionBar?.setDisplayShowTitleEnabled(true)
        toolbar.title = title
    }

    override fun onSuccess(apis: List<ApiController.Api>) {
        onUiThread {
            updateLastUpdated(databaseController.updateDate)
            toast(R.string.refreshed)
            updateFragment(apis)
        }
    }

    override fun onFail(error: UpdateError) {
        if (error == UpdateError.Login)
            logout()
        else {
            if (error == UpdateError.Connection)
                toast(R.string.no_connection)
            errorFragment(error)
        }
    }

    companion object {
        private const val CallbackId = 75
        private val HourFormatter = SimpleDateFormat("HH:mm")
        const val Key_Fragment = "key_fragment"
        private val RegulationFile = File("/data/data/com.yoavst.changesystemohelshem/files/regulation.pdf")
        private val RegulationFilename = "regulation.pdf"
        private val ColorUnselected = Color.parseColor("#727272")
    }
}