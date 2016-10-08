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

package com.ohelshem.app.android

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatDelegate
import android.view.*
import android.widget.ImageView
import android.widget.Spinner
import com.github.salomonbrys.kodein.instance
import com.google.firebase.iid.FirebaseInstanceId
import com.hannesdorfmann.mosby.mvp.MvpFragment
import com.jakewharton.processphoenix.ProcessPhoenix
import com.ohelshem.api.model.UpdateError
import com.ohelshem.app.android.changes.ClassChangesFragment
import com.ohelshem.app.android.changes.LayerChangesFragment
import com.ohelshem.app.android.dashboard.DashboardFragment
import com.ohelshem.app.android.help.HelpActivity
import com.ohelshem.app.android.holidays.HolidaysFragment
import com.ohelshem.app.android.login.LoginActivity
import com.ohelshem.app.android.main.ScreenType
import com.ohelshem.app.android.main.TopNavigationScreenManager
import com.ohelshem.app.android.settings.SettingsActivity
import com.ohelshem.app.android.tests.TestsFragment
import com.ohelshem.app.android.timetable.TimetableFragment
import com.ohelshem.app.android.utils.AppThemedActivity
import com.ohelshem.app.android.utils.DebugMenuSwitchAction
import com.ohelshem.app.controller.api.ApiController
import com.ohelshem.app.controller.storage.DeveloperOptions
import com.ohelshem.app.controller.storage.IStorage
import com.ohelshem.app.model.ApiUpdatable
import com.yoavst.changesystemohelshem.R
import io.palaima.debugdrawer.DebugDrawer
import io.palaima.debugdrawer.actions.ActionsModule
import io.palaima.debugdrawer.actions.ButtonAction
import io.palaima.debugdrawer.actions.SpinnerAction
import io.palaima.debugdrawer.commons.BuildModule
import io.palaima.debugdrawer.commons.DeviceModule
import io.palaima.debugdrawer.commons.NetworkModule
import io.palaima.debugdrawer.commons.SettingsModule
import kotlinx.android.synthetic.main.main.*
import kotlinx.android.synthetic.main.main_drawer_header.view.*
import org.jetbrains.anko.*
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppThemedActivity(), ApiController.Callback, TopNavigationScreenManager {
    private val apiController: ApiController by instance()

    private var lastUpdate: Long = 0
    private var queuedId: Int = -1

    private val layer: Int by lazy { storage.userData.layer }
    private val layerText: String by lazy { stringArrayRes(R.array.layers)[layer - 9] }
    private val headerText: String by lazy { getString(R.string.clazz) + " " + layerText + "'" + storage.userData.clazz.toString() }

    private var fragmentStack: Stack<ScreenType> = Stack()
    private var miniDrawerItems: Array<ImageView>? = null

    private val selectedColor by lazy { primaryColor }

    private lateinit var updatedAt: String
    private lateinit var headerView: View

    private var debugDrawer: DebugDrawer? = null

    //region Activity events
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!storage.isSetup()) {
            startActivity<LoginActivity>()
            overridePendingTransition(0, 0)
            finish()
        } else {
            setContentView(R.layout.main)
            initDrawer()
            lastUpdate = storage.updateDate
            if (savedInstanceState == null) {
                if (resources.getBoolean(R.bool.dashboard_as_default))
                    setScreen(ScreenType.Dashboard)
                else setScreen(ScreenType.Timetable)
                refresh()
            } else setScreen(ScreenType.values()[savedInstanceState.getInt(Key_Fragment)])
            if (extraFragment != null)
                supportFragmentManager.beginTransaction().replace(R.id.extraFragment, DashboardFragment()).commit()
            if (secondaryExtraFragment != null)
                supportFragmentManager.beginTransaction().replace(R.id.secondaryExtraFragment, TestsFragment()).commit()

            debug()

            showIntro()
        }
    }

    override fun onResume() {
        super.onResume()
        debugDrawer?.onResume()
        apiController[CallbackId] = this
        if (lastUpdate != storage.updateDate) {
            lastUpdate = storage.updateDate
            updateFragment()
        }
    }

    override fun onPause() {
        super.onPause()
        debugDrawer?.onPause()
        apiController -= CallbackId
    }

    override fun onStart() {
        super.onStart()
        debugDrawer?.onStart()
    }


    override fun onStop() {
        super.onStop()
        debugDrawer?.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(Key_Fragment, fragmentStack.peek().ordinal)
    }

    override fun onBackPressed() {
        if (fragmentStack.size > 1) {
            fragmentStack.pop() // current fragment
            setScreen(fragmentStack.peek())
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
            classText.text = layerText + "'" + storage.userData.clazz.toString()
            miniDrawerItems = arrayOf(changesDrawerItem.getChildAt(0) as ImageView, timetableDrawerItem.getChildAt(0) as ImageView,
                    layerChangesDrawerItem.getChildAt(0) as ImageView, holidaysDrawerItem.getChildAt(0) as ImageView)
            changesDrawerItem.onClick {
                setScreen(ScreenType.Changes)
            }
            timetableDrawerItem.onClick {
                setScreen(ScreenType.Timetable)
            }
            layerChangesDrawerItem.onClick {
                setScreen(ScreenType.LayerChanges)
            }
            regulationsDrawerItem.onClick {
                openRegulations()
            }
            helpDrawerItem.onClick {
                openHelp()
            }
            holidaysDrawerItem.onClick {
                setScreen(ScreenType.Holidays)
            }
            settingsDrawerItem.onClick {
                openSettings()
            }
        } else {
            // If there is drawer layout
            if (navigationView != null) {
                doFromSdk(Build.VERSION_CODES.LOLLIPOP) {
                    setTranslucentStatusFlag(true)
                }
            }
            updatedAt = getString(R.string.updated_at)
            navigationView.menu.findItem(R.id.layer).title = getString(R.string.layer_changes) + " " + layerText
            headerView = navigationView.inflateHeaderView(R.layout.main_drawer_header).apply {
                classText.text = headerText
            }
            navigationView.setNavigationItemSelectedListener {
                queuedId = it.itemId
                drawerLayout.closeDrawer(navigationView)
                true
            }
            toolbar.navigationIcon = drawableRes(R.drawable.ic_menu)
            toolbar.setNavigationOnClickListener {
                drawerLayout.openDrawer(Gravity.LEFT)
            }
            drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
                override fun onDrawerClosed(drawerView: View?) {
                    if (queuedId != -1) {
                        when (queuedId) {
                            R.id.dashboard -> setScreen(ScreenType.Dashboard)
                            R.id.changes -> setScreen(ScreenType.Changes)
                            R.id.timetable -> setScreen(ScreenType.Timetable)
                            R.id.tests -> setScreen(ScreenType.Tests)
                            R.id.layer -> setScreen(ScreenType.LayerChanges)
                            R.id.holidays -> setScreen(ScreenType.Holidays)
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
        val lastUpdate = storage.updateDate
        if (lastUpdate != IStorage.EmptyData.toLong())
            updateLastUpdated(lastUpdate)
    }

    private fun setTranslucentStatusFlag(on: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val win = window
            val winParams = win.attributes
            val bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
            if (on) {
                winParams.flags = winParams.flags or bits
            } else {
                winParams.flags = winParams.flags and bits.inv()
            }
            win.attributes = winParams
        }
    }


    private fun setSelected(type: ScreenType) {
        if (miniDrawerItems != null) {
            miniDrawerItems!!.forEach { it.setColorFilter(ColorUnselected) }
            if (type == ScreenType.Changes)
                miniDrawerItems!![0].setColorFilter(selectedColor)
            else if (type == ScreenType.Timetable)
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
        if (!RegulationFile.exists()) {
            resources.openRawResource(R.raw.regulations).use { regulationStream ->
                RegulationFile.createNewFile()
                FileOutputStream(RegulationFile).use {
                    regulationStream.copyTo(it)
                }
            }
        }
        val intent = Intent(Intent.ACTION_VIEW).setDataAndType(Uri.parse("content://$packageName/$SharingFolder/$RegulationFilename"), "application/pdf").setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        if (packageManager.queryIntentActivities(intent, 0).isEmpty()) {
            toast("No PDF reader installed")
        } else startActivity(Intent.createChooser(intent, getString(R.string.choose_opener)))
    }

    private fun updateLastUpdated(time: Long) {
        headerView.lastUpdated.text = updatedAt + HourFormatter.format(Date(time))
    }
    //endregion

    fun logout() {
        storage.clean()
        analytics.onLogout()
        startActivity(intentFor<LoginActivity>().clearTask())
        finish()
    }

    private fun getUpdatableFragments(): List<ApiUpdatable> {
        @Suppress("UNCHECKED_CAST")
        return listOf(supportFragmentManager.findFragmentById(R.id.fragment) as? ApiUpdatable,
                supportFragmentManager.findFragmentById(R.id.extraFragment) as? ApiUpdatable,
                supportFragmentManager.findFragmentById(R.id.secondaryExtraFragment) as? ApiUpdatable).filterNotNull()
    }

    private fun updateFragment(apis: Set<ApiController.UpdatedApi>? = null) {
        getUpdatableFragments().forEach { fragment ->
            if (apis == null || fragment.api == null || fragment.api!! in apis) {
                fragment.onUpdate()
            }
        }
    }

    private fun errorFragment(updateError: UpdateError) {
        getUpdatableFragments().forEach { fragment ->
            fragment.onError(updateError)
        }
    }


    override fun refresh(): Boolean {
        val result = apiController.update()
        if (result) {
            // FIXME apply refresh state
        }
        return result
    }


    override fun onSuccess(apis: Set<ApiController.UpdatedApi>) {
        runOnUiThread {
            updateLastUpdated(storage.updateDate)
            toast(R.string.refreshed)
            updateFragment(apis)
            updatables.forEach { it.onSuccess(apis) }
        }
    }

    override fun onFail(error: UpdateError) {
        if (error == UpdateError.Login)
            logout()
        else {
            if (error == UpdateError.Connection)
                toast(R.string.no_connection)
            errorFragment(error)
            updatables.forEach { it.onFail(error) }
        }
    }

    override fun setScreen(screen: ScreenType, backStack: Boolean) {
        val fragment: Fragment = when (screen) {
            ScreenType.Dashboard -> DashboardFragment()
            ScreenType.Changes -> ClassChangesFragment()
            ScreenType.Timetable -> TimetableFragment()
            ScreenType.Tests -> TestsFragment()
            ScreenType.LayerChanges -> LayerChangesFragment()
            ScreenType.Holidays -> HolidaysFragment()
            else -> DashboardFragment()
        }
        supportFragmentManager.beginTransaction().replace(R.id.fragment, fragment).commit()
        if (!backStack)
            fragmentStack.clear()
        fragmentStack.add(screen)
        setSelected(screen)
    }

    override var screenTitle: CharSequence
        get() = toolbar.title
        set(value) {
            navigationSpinner.adapter = null
            navigationSpinner.hide()
            supportActionBar?.setDisplayShowTitleEnabled(true)
            toolbar.title = value
        }

    override val topNavigationElement: Spinner
        get() {
            toolbar.title = ""
            supportActionBar?.setDisplayShowTitleEnabled(false)
            navigationSpinner.show()
            return navigationSpinner
        }

    override fun setToolbarElevation(enabled: Boolean) {
        doFromSdk(Build.VERSION_CODES.LOLLIPOP) {
            if (enabled) {
                toolbar.elevation = 4f
            } else {
                toolbar.elevation = 0f
            }
        }
    }

    val updatables: List<ApiController.Callback>
        get() {
            @Suppress("UNCHECKED_CAST")
            return listOf(
                    (supportFragmentManager.findFragmentById(R.id.fragment) as? MvpFragment<*, *>)?.getPresenter() as? ApiController.Callback,
                    (supportFragmentManager.findFragmentById(R.id.extraFragment) as? MvpFragment<*, *>)?.getPresenter() as? ApiController.Callback,
                    (supportFragmentManager.findFragmentById(R.id.secondaryExtraFragment) as? MvpFragment<*, *>)?.getPresenter() as? ApiController.Callback)
                    .filterNotNull()
        }

    private fun debug() {
        if (storage.developerMode) {
            val nightModeAction = DebugMenuSwitchAction("Night mode", storage.darkMode == AppCompatDelegate.MODE_NIGHT_YES) {
                storage.darkMode = if (it) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            }

            val debugFlagAction = DebugMenuSwitchAction("Debug flag", storage.debugFlag) {
                storage.debugFlag = it
            }

            val restartAction = ButtonAction("Restart app") {
                ProcessPhoenix.triggerRebirth(this)
            }

            val shareFirebaseTokenAction = ButtonAction("Share firebaseToken") {
                toast(FirebaseInstanceId.getInstance().token ?: "No token available")
                startActivity(
                        Intent(Intent.createChooser(
                                Intent(Intent.ACTION_SEND)
                                        .putExtra(Intent.EXTRA_TEXT, FirebaseInstanceId.getInstance().token)
                                        .setType("text/plain"), "Share text")

                        )
                )
            }

            val fakingOptions = listOf("Real info", "Fake changes", "Fake no changes")
            val fakingAction = SpinnerAction(fakingOptions) { value ->
                when (fakingOptions.indexOf(value)) {
                    0 -> DeveloperOptions.stopFaking()
                    1 -> DeveloperOptions.fakeChanges()
                    2 -> DeveloperOptions.fakeNoChanges()

                }
            }

            debugDrawer = DebugDrawer.Builder(this).modules(
                    ActionsModule(debugFlagAction, fakingAction, nightModeAction, restartAction, shareFirebaseTokenAction),
                    DeviceModule(this),
                    BuildModule(this),
                    NetworkModule(this),
                    SettingsModule(this)).build()
        }
    }

    private fun showIntro() {
        if (storage.firstTimeInApp) {
            if (drawerLayout != null) {
                var prompt: MaterialTapTargetPrompt? = null
                prompt = MaterialTapTargetPrompt.Builder(this)
                        .setPrimaryText(R.string.intro_drawer_primary_text)
                        .setSecondaryText(R.string.intro_drawer_secondary_text)
                        .setIcon(R.drawable.ic_menu2)
                        .setTarget(toolbar.getChildAt(1))
                        .setBackgroundColour(primaryDarkColor)
                        .setIconDrawableColourFilter(act.primaryDarkColor)
                        .setAutoDismiss(false)
                        .setOnHidePromptListener(object : MaterialTapTargetPrompt.OnHidePromptListener {
                            override fun onHidePromptComplete() {
                                storage.firstTimeInApp = false
                            }

                            override fun onHidePrompt(event: MotionEvent?, tappedTarget: Boolean) {
                                if (tappedTarget) {
                                    prompt!!.dismiss()
                                }
                            }

                        }).show()
            }
        }
    }

    private val RegulationFile by lazy { File(File(filesDir, SharingFolder).apply { mkdirs() }, RegulationFilename) }

    companion object {
        private const val CallbackId = 75
        private val HourFormatter = SimpleDateFormat("HH:mm")
        private val RegulationFilename = "regulation.pdf"
        private val SharingFolder = "sharing"
        const val Key_Fragment = "key_fragment"
        private val ColorUnselected = Color.parseColor("#727272")
    }
}