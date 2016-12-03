package com.ohelshem.app.android.main

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.TabLayout
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatDelegate
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.Spinner
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog
import com.github.javiersantos.materialstyleddialogs.enums.Style
import com.github.salomonbrys.kodein.erased.instance
import com.google.firebase.iid.FirebaseInstanceId
import com.hannesdorfmann.mosby.mvp.MvpFragment
import com.jakewharton.processphoenix.ProcessPhoenix
import com.ohelshem.api.model.ClassInfo
import com.ohelshem.api.model.UpdateError
import com.ohelshem.app.android.*
import com.ohelshem.app.android.changes.ChangesFragment
import com.ohelshem.app.android.contacts.ContactsFragment
import com.ohelshem.app.android.dashboard.DashboardFragment
import com.ohelshem.app.android.dates.DatesFragment
import com.ohelshem.app.android.dates.list.DatesListFragment
import com.ohelshem.app.android.help.HelpActivity
import com.ohelshem.app.android.login.ActiveClassDialog
import com.ohelshem.app.android.login.LoginActivity
import com.ohelshem.app.android.notifications.ChangesNotificationGenerator
import com.ohelshem.app.android.notifications.OngoingNotificationService
import com.ohelshem.app.android.settings.SettingsActivity
import com.ohelshem.app.android.timetable.TimetableFragment
import com.ohelshem.app.android.utils.AppThemedActivity
import com.ohelshem.app.android.utils.BadgeBarGenerator
import com.ohelshem.app.android.utils.BaseMvpFragment
import com.ohelshem.app.android.utils.DebugMenuSwitchAction
import com.ohelshem.app.controller.analytics.Analytics
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
import kotlinx.android.synthetic.main.teacher_badge_layout.*
import me.tabak.fragmentswitcher.FragmentArrayPagerAdapter
import org.jetbrains.anko.*
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.comparisons.compareBy


class MainActivity : AppThemedActivity(), ApiController.Callback, TopNavigationScreenManager {
    private val apiController: ApiController by instance()

    private var lastUpdate: Long = 0

    private var fragmentStack: Stack<ScreenType> = Stack()

    private var debugDrawer: DebugDrawer? = null

    private var firstUpdate: Boolean = true

    private val analyticsManager: Analytics by kodein.instance()

    private val notificationCallback = { title: String, body: String -> onNotification(title, body) }
    private var dialog: MaterialStyledDialog? = null

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
            initTeacherBar()
            lastUpdate = storage.updateDate

            when (intent?.action) {
                Shortcut_LaunchChanges -> setScreen(ScreenType.Changes)
                Shortcut_LaunchTimetable -> setScreen(ScreenType.Timetable)
                Shortcut_LaunchDates -> setScreen(ScreenType.Dates)
                Shortcut_LaunchMyClass -> setScreen(ScreenType.Contacts)
                else -> {
                    if (intent?.action == Action_Notification) {
                        onNotification(intent.getStringExtra(Intent.EXTRA_TITLE), intent.getStringExtra(Intent.EXTRA_TEXT))
                    }

                    if (savedInstanceState == null) {
                        if (resources.getBoolean(R.bool.dashboard_as_default))
                            setScreen(ScreenType.Dashboard)
                        else setScreen(ScreenType.Timetable)
                        refresh()
                    } else setScreen(ScreenType.values()[savedInstanceState.getInt(Key_Fragment)])
                }
            }
            if (extraFragment != null)
                supportFragmentManager.beginTransaction().replace(R.id.extraFragment, DashboardFragment()).commit()
            if (secondaryExtraFragment != null)
                supportFragmentManager.beginTransaction().replace(R.id.secondaryExtraFragment, DatesListFragment()).commit()

            debug()
            if (storage.userData.isTeacher()) {
                storage.notificationsForChanges = false
                storage.notificationsForTests = false
                if (storage.primaryClass == null)
                    storage.notificationsForBirthdays = false
            }
            analyticsManager.onLogin()
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
        App.messageCallback = notificationCallback
        updateBadges()
    }

    override fun onPause() {
        super.onPause()
        debugDrawer?.onPause()
        App.messageCallback = null
        apiController -= CallbackId
        dialog?.dismiss()
        dialog = null
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
        outState.putInt(Key_Fragment, fragmentPosition[fragmentStack.peek()]!!)
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
    private val fragmentPosition: Map<ScreenType, Int> = mapOf(ScreenType.Dashboard to 4, ScreenType.Timetable to 3, ScreenType.Changes to 2, ScreenType.Dates to 1, ScreenType.Contacts to 0)

    //positions are flipped to allow RTL bottom bar layout
    private fun initNavigation() {
        toolbar.title = ""
        setSupportActionBar(toolbar)
        fragmentSwitcher.adapter = FragmentArrayPagerAdapter<Fragment>(supportFragmentManager).apply {
            addAll(
                    ContactsFragment(),
                    DatesFragment(),
                    ChangesFragment(),
                    TimetableFragment(),
                    DashboardFragment()
            )

        }
        bottomBar.setOnTabSelectListener { id ->
            when (id) {
                R.id.dashboard -> setScreenInternal(ScreenType.Dashboard)
                R.id.timetable -> setScreenInternal(ScreenType.Timetable)
                R.id.changes -> setScreenInternal(ScreenType.Changes)
                R.id.dates -> setScreenInternal(ScreenType.Dates)
                R.id.contacts -> setScreenInternal(ScreenType.Contacts)
            }
        }
        bottomBar.setOnTabReselectListener {
            notifyFragmentOnReselect()
        }

        if (resources.getBoolean(R.bool.isTablet)) {
            bottomBar.isSaveEnabled = false
        }
    }

    private fun initTeacherBar() {
        if (storage.userData.isTeacher()) {
            var classes = storage.classes
            classes = classes.sortedWith(compareBy({ it.layer }, { it.clazz })).reversed()

            val primaryText = storage.primaryClass?.let {
                classes -= it
                "${stringArrayRes(R.array.layers)[it.layer - 9]}'${it.clazz}"
            }

            BadgeBarGenerator.inflate(teacherBar, classes, primaryText, getString(R.string.personal), storage.primaryClass, null, null, {
                BadgeBarGenerator.badgesDisableAll(teacherBar)
                val schoolClasses = getSchoolClasses()
                val newClassDialog = ActiveClassDialog.create(this, schoolClasses)
                var chosenClass = ClassInfo(0,0)
                newClassDialog.itemsCallbackSingleChoice(0) { dialog, view, which, text ->
                    chosenClass = schoolClasses[which] //FIXME
                    true
                }
                newClassDialog.onPositive { materialDialog, dialogAction ->  notifyFragmentOnChooseClass(if (chosenClass.layer != 0) chosenClass else currentClass)}
                newClassDialog.build().show()
            }) {
                notifyFragmentOnChooseClass(it)
            }
            teacherBar.show()
        } else {
            teacherBar.hide()
        }
    }

    override fun setScreen(screen: ScreenType, backStack: Boolean) {
        if (!backStack)
            fragmentStack.clear()
        fragmentStack.add(screen)
        setSelected(screen)
    }

    private fun setScreenInternal(screen: ScreenType) {
        fragmentSwitcher.currentItem = fragmentPosition[screen]!!
        (fragmentSwitcher.currentFragment as? BaseMvpFragment<*, *>)?.onBecomingVisible()
        if (!storage.isStudent()) {
            if (screen == ScreenType.Dashboard) teacherBar.hide() else teacherBar.show()
        }
    }

    private fun setSelected(screen: ScreenType) {
        bottomBar.selectTabAtPosition(fragmentPosition[screen]!!)
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
        analytics.onLogout()
        storage.clean()
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
            if (!firstUpdate) {
                toast(R.string.refreshed)
            } else {
                firstUpdate = false
            }
            updatables.forEach { it.onSuccess(apis) }
            OngoingNotificationService.update(applicationContext)
            updateBadges()
        }
    }

    override fun onFail(error: UpdateError) {
        if (error == UpdateError.Login)
            logout()
        else if (error == UpdateError.Connection) {
            toast(R.string.no_connection)
            updatables.forEach { it.onFail(error) }
        } else
            toast(R.string.refresh_fail)
    }

    fun onNotification(title: String, body: String) {
        runOnUiThread {
            dialog = MaterialStyledDialog.Builder(this)
                    .setTitle(title)
                    .setDescription(body.fromHtml())
                    .autoDismiss(true)
                    .setPositiveText(R.string.accept)
                    .onPositive { materialDialog, dialogAction ->
                        materialDialog.cancel()
                    }.show()
        }
    }


    //region Utils
    override var screenTitle: CharSequence
        get() = toolbar.title
        set(value) {
            navigationSpinner.adapter = null
            navigationSpinner.hide()
            tabs.removeAllTabs()
            tabs.hide()
            supportActionBar?.setDisplayShowTitleEnabled(true)
            toolbar.title = value
        }

    override val topNavigationElement: Spinner
        get() {
            tabs.removeAllTabs()
            tabs.hide()
            toolbar.title = ""
            supportActionBar?.setDisplayShowTitleEnabled(false)
            navigationSpinner.show()
            return navigationSpinner
        }

    override val inlineTabs: TabLayout
        get() {
            navigationSpinner.adapter = null
            navigationSpinner.hide()
            toolbar.title = ""
            supportActionBar?.setDisplayShowTitleEnabled(false)
            tabs.removeAllTabs()
            tabs.show()
            return tabs
        }

    override fun setToolbarElevation(enabled: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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

    fun notifyFragmentOnReselect() {
        (fragmentSwitcher.currentFragment as? BaseMvpFragment<*, *>)?.onReselected()
    }

    fun notifyFragmentOnChooseClass(classInfo: ClassInfo?) {
        currentClass = classInfo
        (fragmentSwitcher.currentFragment as? BaseMvpFragment<*, *>)?.onChoosingClass(classInfo)
    }

    override var currentClass: ClassInfo? = null

    val updatables: List<ApiController.Callback>
        get() {
            @Suppress("UNCHECKED_CAST")
            return listOf(
                    (fragmentSwitcher.currentFragment as? MvpFragment<*, *>)?.getPresenter() as? ApiController.Callback,
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

            val sendNotificationAction = ButtonAction("Send changes notification") {
                Handler().postDelayed({ ChangesNotificationGenerator(this).prepareNotification() }, 1000)
                val quit = Intent(Intent.ACTION_MAIN)
                quit.addCategory(Intent.CATEGORY_HOME)
                quit.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(quit)
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
                    ActionsModule(debugFlagAction, fakingAction, nightModeAction, restartAction, sendNotificationAction, shareFirebaseTokenAction),
                    DeviceModule(),
                    BuildModule(),
                    NetworkModule(),
                    SettingsModule()).build()
        }
    }
    //endregion

    //region Intro
    private fun showIntro() {
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

    override fun startTour() {
        introTimetable()
    }

    private fun introTimetable() {
        var prompt: MaterialTapTargetPrompt? = null
        prompt = MaterialTapTargetPrompt.Builder(this)
                .setPrimaryText(R.string.intro_bottombar_timetable_primary_text)
                .setSecondaryText(if (storage.isStudent()) R.string.intro_bottombar_timetable_secondary_text else R.string.intro_bottombar_timetable_secondary_text_teacher)
                .setTarget(bottomBar.getTabWithId(R.id.timetable))
                .setIcon(R.drawable.ic_timetable_blue)
                .setBackgroundColour(act.primaryColor)
                .setCaptureTouchEventOutsidePrompt(true)
                .setAutoDismiss(false)
                .setIconDrawableColourFilter(act.primaryDarkColor)
                .setOnHidePromptListener(object : MaterialTapTargetPrompt.OnHidePromptListener {

                    override fun onHidePromptComplete() {
                        introChanges()
                    }

                    override fun onHidePrompt(event: MotionEvent?, tappedTarget: Boolean) {
                        if (tappedTarget) {
                            prompt?.finish()
                        }
                    }
                }).show()
    }

    private fun introChanges() {
        var prompt: MaterialTapTargetPrompt? = null
        prompt = MaterialTapTargetPrompt.Builder(this)
                .setPrimaryText(R.string.intro_bottombar_changes_primary_text)
                .setSecondaryText(if (storage.isStudent()) R.string.intro_bottombar_changes_secondary_text else R.string.intro_bottombar_changes_secondary_text_teacher)
                .setTarget(bottomBar.getTabWithId(R.id.changes))
                .setIcon(R.drawable.ic_track_changes_blue)
                .setBackgroundColour(act.primaryColor)
                .setCaptureTouchEventOutsidePrompt(true)
                .setAutoDismiss(false)
                .setIconDrawableColourFilter(act.primaryDarkColor)
                .setOnHidePromptListener(object : MaterialTapTargetPrompt.OnHidePromptListener {

                    override fun onHidePromptComplete() {
                        introDates()
                    }

                    override fun onHidePrompt(event: MotionEvent?, tappedTarget: Boolean) {
                        if (tappedTarget) {
                            prompt?.finish()
                        }
                    }
                }).show()
    }

    private fun introDates() {
        var prompt: MaterialTapTargetPrompt? = null
        prompt = MaterialTapTargetPrompt.Builder(this)
                .setPrimaryText(R.string.intro_bottombar_dates_primary_text)
                .setSecondaryText(if (storage.isStudent()) R.string.intro_bottombar_dates_secondary_text else R.string.intro_bottombar_dates_secondary_text_teacher)
                .setTarget(bottomBar.getTabWithId(R.id.dates))
                .setIcon(R.drawable.ic_calendar_blue)
                .setBackgroundColour(act.primaryColor)
                .setCaptureTouchEventOutsidePrompt(true)
                .setAutoDismiss(false)
                .setIconDrawableColourFilter(act.primaryDarkColor)
                .setOnHidePromptListener(object : MaterialTapTargetPrompt.OnHidePromptListener {

                    override fun onHidePromptComplete() {
                        introContacts()
                    }

                    override fun onHidePrompt(event: MotionEvent?, tappedTarget: Boolean) {
                        if (tappedTarget) {
                            prompt?.finish()
                        }
                    }
                }).show()
    }

    private fun introContacts() {
        var prompt: MaterialTapTargetPrompt? = null
        prompt = MaterialTapTargetPrompt.Builder(this)
                .setPrimaryText(R.string.intro_bottombar_contacts_primary_text)
                .setSecondaryText(if (storage.isStudent()) R.string.intro_bottombar_contacts_secondary_text else R.string.intro_bottombar_contacts_secondary_text_teacher)
                .setTarget(bottomBar.getTabWithId(R.id.contacts))
                .setIcon(R.drawable.ic_contacts)
                .setBackgroundColour(act.primaryColor)
                .setCaptureTouchEventOutsidePrompt(true)
                .setAutoDismiss(false)
                .setIconDrawableColourFilter(act.primaryDarkColor)
                .setOnHidePromptListener(object : MaterialTapTargetPrompt.OnHidePromptListener {

                    override fun onHidePromptComplete() {
                        introMenu()
                    }

                    override fun onHidePrompt(event: MotionEvent?, tappedTarget: Boolean) {
                        if (tappedTarget) {
                            prompt?.finish()
                        }
                    }
                }).show()
    }

    private fun introMenu() {
        val menuItem = toolbar.getChildAt(2).childrenSequence().last()
        var prompt: MaterialTapTargetPrompt? = null
        prompt = MaterialTapTargetPrompt.Builder(this)
                .setPrimaryText(R.string.intro_menu_primary_text)
                .setSecondaryText(R.string.intro_menu_secondary_text)
                .setTarget(menuItem)
                .setIconDrawable(VectorDrawableCompat.create(resources, R.drawable.abc_ic_menu_overflow_material, theme))
                .setBackgroundColour(act.primaryColor)
                .setAutoDismiss(false)
                .setIconDrawableColourFilter(act.primaryDarkColor)
                .setOnHidePromptListener(object : MaterialTapTargetPrompt.OnHidePromptListener {

                    override fun onHidePromptComplete() {
                    }

                    override fun onHidePrompt(event: MotionEvent?, tappedTarget: Boolean) {
                        if (tappedTarget) {
                            prompt?.finish()
                            storage.firstTimeInApp = false
                        }
                    }
                })
                .show()
    }

//endregion

    private
    val RegulationFile by lazy { File(File(filesDir, SharingFolder).apply { mkdirs() }, RegulationFilename) }

    companion object {
        private const val CallbackId = 75
        private val RegulationFilename = "regulation.pdf"
        val SharingFolder = "sharing"
        private const val Key_Fragment = "key_fragment"

        const val Shortcut_LaunchChanges = "com.ohelshem.app.LAUNCH_CHANGES"
        const val Shortcut_LaunchTimetable = "com.ohelshem.app.LAUNCH_TIMETABLE"
        const val Shortcut_LaunchDates = "com.ohelshem.app.LAUNCH_DATES"
        const val Shortcut_LaunchMyClass = "com.ohelshem.app.LAUNCH_MY_CLASS"
        const val Action_Notification = "com.ohelshem.app.NOTIFICATION"

    }
}
