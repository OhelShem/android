package com.ohelshem.app.android.main

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatDelegate
import android.view.Menu
import android.view.MenuItem
import android.widget.Spinner
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog
import com.github.javiersantos.materialstyleddialogs.enums.Style
import com.github.salomonbrys.kodein.instance
import com.google.firebase.iid.FirebaseInstanceId
import com.hannesdorfmann.mosby.mvp.MvpFragment
import com.jakewharton.processphoenix.ProcessPhoenix
import com.ohelshem.api.model.UpdateError
import com.ohelshem.app.android.App
import com.ohelshem.app.android.changes.ClassChangesFragment
import com.ohelshem.app.android.changes.LayerChangesFragment
import com.ohelshem.app.android.dashboard.DashboardFragment
import com.ohelshem.app.android.help.HelpActivity
import com.ohelshem.app.android.hide
import com.ohelshem.app.android.login.LoginActivity
import com.ohelshem.app.android.settings.SettingsActivity
import com.ohelshem.app.android.show
import com.ohelshem.app.android.tests.TestsFragment
import com.ohelshem.app.android.timetable.TimetableFragment
import com.ohelshem.app.android.utils.AppThemedActivity
import com.ohelshem.app.android.utils.DebugMenuSwitchAction
import com.ohelshem.app.controller.api.ApiController
import com.ohelshem.app.controller.storage.DeveloperOptions
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
import org.jetbrains.anko.*
import java.io.File
import java.io.FileOutputStream
import java.util.*

class MainActivity : AppThemedActivity(), ApiController.Callback, TopNavigationScreenManager {
    private val apiController: ApiController by instance()

    private var lastUpdate: Long = 0

    private var fragmentStack: Stack<ScreenType> = Stack()

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
            initNavigation()
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
            updatables.forEach { it.onSuccess() }
        }
        updateBadges()
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.refresh -> {
                refresh()
                true
            }
            R.id.regulations -> {
                openRegulations()
                true
            }
            R.id.help -> {
                openHelp()
                true
            }
            R.id.settings -> {
                openSettings()
                true
            }
            else -> false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 42 && resultCode == 42) {
            logout()
        }
    }
    //endregion

    //region Navigation
    private fun initNavigation() {
        toolbar.title = ""
        setSupportActionBar(toolbar)
        bottomBar.setOnTabSelectListener { id ->
            when (id) {
                R.id.dashboard -> setScreenInternal(ScreenType.Dashboard)
                R.id.timetable -> setScreenInternal(ScreenType.Timetable)
                R.id.changes -> setScreenInternal(ScreenType.Changes)
                R.id.dates -> setScreenInternal(ScreenType.Dates)
                R.id.contacts -> setScreenInternal(ScreenType.Contacts)
            }
        }
    }

    override fun setScreen(screen: ScreenType, backStack: Boolean) {
        if (!backStack)
            fragmentStack.clear()
        fragmentStack.add(screen)
        setSelected(screen)
    }

    private fun setScreenInternal(screen: ScreenType) {
        val fragment: Fragment = when (screen) {
            ScreenType.Dashboard -> DashboardFragment()
            ScreenType.Changes -> ClassChangesFragment()
            ScreenType.Timetable -> TimetableFragment()
            ScreenType.Dates -> TestsFragment()
            ScreenType.Contacts -> LayerChangesFragment()
            else -> DashboardFragment()
        }
        supportFragmentManager.beginTransaction().replace(R.id.fragment, fragment).commit()
    }

    private fun setSelected(type: ScreenType) {
        bottomBar.selectTabAtPosition(type.ordinal)
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
//endregion

    fun logout() {
        storage.clean()
        analytics.onLogout()
        startActivity(intentFor<LoginActivity>().clearTask())
        finish()
    }

    override fun refresh(): Boolean {
        val result = apiController.update()
        if (result) {
            // TODO apply refresh state
        }
        return result
    }


    override fun onSuccess(apis: Set<ApiController.UpdatedApi>) {
        runOnUiThread {
            toast(R.string.refreshed)
            updatables.forEach { it.onSuccess(apis) }
            updateBadges()
        }
    }

    override fun onFail(error: UpdateError) {
        if (error == UpdateError.Login)
            logout()
        else {
            if (error == UpdateError.Connection)
                toast(R.string.no_connection)
            updatables.forEach { it.onFail(error) }
        }
    }


    //region Utils
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
                appBarLayout.elevation = 4f
            } else {
                appBarLayout.elevation = 0f
            }
        }
    }

    private fun updateBadges() {
        val changesTab = bottomBar.getTabWithId(R.id.changes)

        val clazz = storage.userData.clazz
        val count = storage.changes?.count { it.clazz == clazz } ?: 0
        if (count == 0)
            changesTab.removeBadge()
        else changesTab.setBadgeCount(count)
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
    //endregion

    private fun showIntro() {
        if (storage.firstTimeInApp) {
            // FIXME
        }
        if (App.updatedFromVersion != -1) {
            App.updatedFromVersion = -1
            MaterialStyledDialog.Builder(this)
                    .setStyle(Style.HEADER_WITH_TITLE)
                    .setTitle(R.string.changelog)
                    .setCustomView(layoutInflater.inflate(R.layout.changelog_dialog_fragment, null, false))
                    .setPositiveText(android.R.string.ok)
                    .show()
        }
    }

    private val RegulationFile by lazy { File(File(filesDir, SharingFolder).apply { mkdirs() }, RegulationFilename) }

    companion object {
        private const val CallbackId = 75
        private val RegulationFilename = "regulation.pdf"
        private val SharingFolder = "sharing"
        const val Key_Fragment = "key_fragment"
    }
}