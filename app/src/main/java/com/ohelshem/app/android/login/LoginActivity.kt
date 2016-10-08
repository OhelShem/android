package com.ohelshem.app.android.login

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.instance
import com.hannesdorfmann.mosby.mvp.MvpActivity
import com.ohelshem.api.model.UpdateError
import com.ohelshem.app.android.*
import com.readystatesoftware.systembartint.SystemBarTintManager
import com.yoavst.changesystemohelshem.R
import kotlinx.android.synthetic.main.login.*
import kotlinx.android.synthetic.main.login_view.*
import org.jetbrains.anko.*

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
        // set background
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val bmOptions = BitmapFactory.Options()
        val scaleFactor = Math.min(1080 / size.x, 1920 / size.y)
        bmOptions.inSampleSize = scaleFactor
        bmOptions.inPreferredConfig = Bitmap.Config.RGB_565
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.entrance_background, bmOptions)
        layout.backgroundDrawable = BitmapDrawable(resources, bitmap)
    }

    fun initViews() {
        registerFab.onClick {
            browse("http://ohel-shem.com/portal6/system/register.php")
        }
        loadingBar.onRepeat {
            onRepeat()
        }
        loginButton.onClick { onLogin() }
        passwordInput.setOnEditorActionListener { textView, actionId, keyEvent ->
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

    private var shouldStopAnimation: Boolean = false
    private fun onRepeat() {
        if (shouldStopAnimation) {
            loadingBar.stopAnim()
            finish()
            startActivity<MainActivity>()
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
