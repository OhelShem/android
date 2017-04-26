package com.ohelshem.app.android.login

import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.erased.instance
import com.hannesdorfmann.mosby3.mvp.MvpActivity
import com.ohelshem.api.model.UpdateError
import com.ohelshem.app.android.hideKeyboard
import com.ohelshem.app.android.invisible
import com.ohelshem.app.android.main.MainActivity
import com.ohelshem.app.android.setMargins
import com.ohelshem.app.android.show
import com.ohelshem.app.controller.storage.Storage
import com.ohelshem.app.controller.storage.TeacherStorage
import com.readystatesoftware.systembartint.SystemBarTintManager
import com.yoavst.changesystemohelshem.R
import kotlinx.android.synthetic.main.login.*
import kotlinx.android.synthetic.main.login_view.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk15.listeners.onClick

class LoginActivity : MvpActivity<LoginView, LoginPresenter>(), LoginView {
    override fun createPresenter(): LoginPresenter = with(appKodein()) { LoginPresenter(instance(), instance(), instance()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)
        initLayout()
        initViews()
        presenter.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }

    fun initLayout() {
        // add the padding from bottom to the layout
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val config = SystemBarTintManager(this).config
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // translucent navigation and status bar by default
                if (!config.isNavigationAtBottom)
                    coordinatorLayout.setMargins(top = config.statusBarHeight)
                else
                    coordinatorLayout.setMargins(top = config.statusBarHeight, bottom = config.navigationBarHeight)
            } else {
                if (!config.isNavigationAtBottom)
                    coordinatorLayout.topPadding = config.statusBarHeight
                else {
                    coordinatorLayout.topPadding = config.statusBarHeight
                    coordinatorLayout.bottomPadding = config.navigationBarHeight
                }
            }
        }
    }

    fun initViews() {
        registerFab.onClick {
            browse("http://ohel-shem.com/portal6/system/register.php")
        }
        loadingBar.onRepeat {
            onRepeat()
        }
        loginButton.onClick { onLogin() }
        passwordInput.setOnEditorActionListener { _, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_GO || (keyEvent.action == KeyEvent.ACTION_DOWN && keyEvent.keyCode == KeyEvent.KEYCODE_ENTER)) {
                onLogin()
                true
            } else false
        }
    }

    override fun showLoading() {
        idInputLayout.error = null
        passwordInputLayout.error = null

        passwordInputLayout.isEnabled = false
        idInputLayout.isEnabled = false
        hideKeyboard()
        loadingBar.show()
        loadingBar.startAnim()
    }

    override fun showLoginError(error: UpdateError) {
        loadingBar.stopAnim()
        loadingBar.invisible()
        passwordInputLayout.isEnabled = true
        idInputLayout.isEnabled = true

        when (error) {
            UpdateError.Connection -> Snackbar.make(coordinatorLayout, R.string.no_connection, Snackbar.LENGTH_SHORT).show()
            UpdateError.Login -> passwordInputLayout.error = getString(R.string.login_wrong)
            UpdateError.NoData -> Snackbar.make(coordinatorLayout, R.string.general_error, Snackbar.LENGTH_SHORT).show()
            UpdateError.Exception -> Snackbar.make(coordinatorLayout, R.string.general_error, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun showIdInvalidError() {
        loadingBar.stopAnim()
        loadingBar.invisible()
        idInputLayout.error = null
        idInputLayout.error = getString(R.string.invalid_id)
    }

    override fun showPasswordInvalidError() {
        loadingBar.stopAnim()
        loadingBar.invisible()
        passwordInputLayout.error = null
        passwordInputLayout.error = getString(R.string.password_too_short)
    }

    override fun launchApp() {
        shouldStopAnimation = true
    }

    override fun showTeachersDialog(teacherStorage: TeacherStorage, listener: () -> Unit) {
        runOnUiThread {
            PrimaryClassDialog.create(teacherStorage, act) {
                listener()
            }
        }
    }

    override fun showStudentsDialog(studentStorage: Storage, listener: () -> Unit) {
        listener()
    }

    private var shouldStopAnimation: Boolean = false
    private fun onRepeat() {
        if (shouldStopAnimation) {
            runOnUiThread {
                loadingBar.stopAnim()
                WelcomeDialog.create(appKodein().instance(), act) {
                    finish()
                    startActivity<MainActivity>()
                }
            }
        }
    }

    fun onLogin() {
        idInputLayout.error = null
        passwordInputLayout.error = null
        val id = idInputLayout.editText!!.text.toString()
        val password = passwordInputLayout.editText!!.text.toString()
        hideKeyboard()
        presenter.login(id, password)
    }

}
