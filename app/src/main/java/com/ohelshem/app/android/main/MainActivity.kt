package com.ohelshem.app.android.main

import android.content.Intent
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
import com.afollestad.materialdialogs.GravityEnum
import com.afollestad.materialdialogs.MaterialDialog
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog
import com.github.javiersantos.materialstyleddialogs.enums.Style
import com.github.salomonbrys.kodein.erased.instance
import com.google.firebase.iid.FirebaseInstanceId
import com.hannesdorfmann.mosby3.mvp.MvpFragment
import com.jakewharton.processphoenix.ProcessPhoenix
import com.ohelshem.api.model.ClassInfo
import com.ohelshem.api.model.UpdateError
import com.ohelshem.app.android.*
import com.ohelshem.app.android.changes.ChangesFragment
import com.ohelshem.app.android.changes.teacher.TeacherChangesFragment
import com.ohelshem.app.android.contacts.ContactsFragment
import com.ohelshem.app.android.dashboard.DashboardFragment
import com.ohelshem.app.android.dates.DatesFragment
import com.ohelshem.app.android.help.HelpActivity
import com.ohelshem.app.android.login.LoginActivity
import com.ohelshem.app.android.notifications.OngoingNotificationService
import com.ohelshem.app.android.settings.SettingsActivity
import com.ohelshem.app.android.timetable.TimetableFragment
import com.ohelshem.app.android.utils.*
import com.ohelshem.app.controller.analytics.Analytics
import com.ohelshem.app.controller.api.ApiController
import com.ohelshem.app.controller.info.SchoolInfo
import com.ohelshem.app.controller.storage.DeveloperOptions
import com.plattysoft.leonids.ParticleSystem
import com.sloydev.preferator.Preferator
import com.yoavst.changesystemohelshem.BuildConfig
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
import org.jetbrains.anko.sdk15.listeners.onClick
import java.util.*


class MainActivity : AppThemedActivity(), ApiController.Callback, TopNavigationScreenManager {
    private val apiController: ApiController by instance()
    private val schoolInfo: SchoolInfo by instance()

    private var lastUpdate: Long = 0

    private var fragmentStack: Stack<ScreenType> = Stack()

    private var debugDrawer: DebugDrawer? = null

    private var firstUpdate: Boolean = true

    private val analyticsManager: Analytics by instance()

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
                    if (storage.isStudent()) ChangesFragment() else TeacherChangesFragment(),
                    TimetableFragment(),
                    DashboardFragment()
            )

        }
        bottomBar.setOnTabSelectListener { id ->
            when (id) {
                R.id.tab_dashboard -> setScreenInternal(ScreenType.Dashboard)
                R.id.tab_timetable -> setScreenInternal(ScreenType.Timetable)
                R.id.tab_changes -> setScreenInternal(ScreenType.Changes)
                R.id.tab_dates -> setScreenInternal(ScreenType.Dates)
                R.id.tab_contacts -> setScreenInternal(ScreenType.Contacts)
            }
        }
        bottomBar.setOnTabReselectListener {
            notifyFragmentOnReselect()
        }
    }

    private fun initTeacherBar() {
        if (storage.userData.isTeacher()) {
            if (findViewById(R.id.storiesBar) != null)
                storiesBar.hide()
            val layers = stringArrayRes(R.array.layers)
            var classes = storage.classes
            classes = classes.sortedWith(compareBy({ it.layer }, { it.clazz })).reversed()

            val primaryText = storage.primaryClass?.let {
                classes -= it
                "${layers[it.layer - 9]}'${it.clazz}"
            }

            BadgeBarGenerator.inflate(teacherBar, classes, primaryText, getString(R.string.personal), storage.primaryClass, null, null, {
                val schoolClasses = schoolInfo.allClasses
                var chosenClass: ClassInfo?

                MaterialDialog.Builder(this)
                        .title(R.string.set_current_class)
                        .autoDismiss(true)
                        .canceledOnTouchOutside(true)
                        .cancelable(true)
                        .itemsGravity(GravityEnum.CENTER)
                        .items(schoolClasses.map { "${layers[it.layer - 9]}'${it.clazz}" })
                        .negativeText(R.string.cancel)
                        .itemsCallback { _, _, which, _ ->
                            chosenClass = schoolClasses[which]
                            BadgeBarGenerator.badgeSelect(teacherBar, chosenClass)
                            notifyFragmentOnChooseClass(chosenClass)
                        }.onNegative { materialDialog, _ ->
                    materialDialog.dismiss()
                }.show()
            }) {
                notifyFragmentOnChooseClass(it)
            }
            teacherBar.show()
        } else {
            teacherBar.hide()
            if (findViewById(R.id.storiesBar) != null) {
                if (storage.debugFlag) {
                    story1.onClick {
                        doStories(R.drawable.story1)
                    }
                    story2.onClick {
                        doStories(R.drawable.story2)
                    }
                    story3.onClick {
                        doStories(R.drawable.story3)

                    }
                    story4.onClick {
                        doStories(R.drawable.story4)
                    }
                    story5.onClick {
                        doStories(R.drawable.story5)

                    }
                } else storiesBar.hide()
            }
        }
    }

    fun doStories(resource: Int) {
        ParticleSystem(this, 80, resource, 10000)
                .setSpeedModuleAndAngleRange(0f, 0.3f, 120, 180)
                .setRotationSpeed(120f)
                .setAcceleration(0.00005f, 60)
                .oneShot(topRight, 10)

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
            if (screen == ScreenType.Dashboard || screen == ScreenType.Changes) teacherBar.hide() else teacherBar.show()
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
//endregion

    fun logout() {
        analytics.onLogout()
        storage.clean()
        startActivity(intentFor<LoginActivity>().clearTask())
        finish()
    }

    override fun refresh(): Boolean {
        return apiController.update()
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
            dialog = MaterialStyledDialog.Builder(act)
                    .setTitle(title)
                    .setDescription(body.fromHtml())
                    .autoDismiss(true)
                    .setPositiveText(R.string.accept)
                    .onPositive { materialDialog, _ ->
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
        val changesTab = bottomBar.getTabWithId(R.id.tab_changes)

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
                    (fragmentSwitcher.currentFragment as? MvpFragment<*, *>)?.getPresenter() as? ApiController.Callback).filterNotNull()
        }

    private fun debug() {
        if (storage.developerMode || BuildConfig.DEBUG) {
            val nightModeAction = DebugMenuSwitchAction("Night mode", storage.darkMode == AppCompatDelegate.MODE_NIGHT_YES) {
                storage.darkMode = if (it) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            }

            val debugFlagAction = DebugMenuSwitchAction("Debug flag", storage.debugFlag) {
                storage.debugFlag = it
            }

            val restartAction = ButtonAction("Restart app") {
                ProcessPhoenix.triggerRebirth(this)
            }

            val preferenceAction = ButtonAction("Preferences editor") {
                numberOfTaps++
                handler.removeCallbacksAndMessages(null)
                if (numberOfTaps == 7) {
                    Preferator.launch(this)
                } else handler.postDelayed(request, 500)
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
                    ActionsModule(debugFlagAction, fakingAction, nightModeAction, restartAction, preferenceAction, shareFirebaseTokenAction),
                    DeviceModule(),
                    BuildModule(),
                    NetworkModule(),
                    SettingsModule()).build()
        }
    }
    private var numberOfTaps: Int = 0
    private val handler = Handler()
    private val request: Runnable = Runnable {
        numberOfTaps = 0
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
                    .setPositiveText(R.string.dialog_close)
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
                .setTarget(bottomBar.getTabWithId(R.id.tab_timetable))
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
                .setTarget(bottomBar.getTabWithId(R.id.tab_changes))
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
                .setTarget(bottomBar.getTabWithId(R.id.tab_dates))
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
                .setTarget(bottomBar.getTabWithId(R.id.tab_contacts))
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

    companion object {
        val SharingFolder = "sharing"
        private const val Key_Fragment = "key_fragment"
        private const val CallbackId = 75

        const val Shortcut_LaunchChanges = "com.ohelshem.app.LAUNCH_CHANGES"
        const val Shortcut_LaunchTimetable = "com.ohelshem.app.LAUNCH_TIMETABLE"
        const val Shortcut_LaunchDates = "com.ohelshem.app.LAUNCH_DATES"
        const val Shortcut_LaunchMyClass = "com.ohelshem.app.LAUNCH_MY_CLASS"
        const val Action_Notification = "com.ohelshem.app.NOTIFICATION"

    }
}
