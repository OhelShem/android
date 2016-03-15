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

package com.ohelshem.app.android.login

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.inputmethod.EditorInfo
import com.hannesdorfmann.mosby.mvp.MvpActivity
import com.ohelshem.api.model.UpdateError
import com.ohelshem.app.android.MainActivity
import com.ohelshem.app.android.util.hideKeyboard
import com.ohelshem.app.android.util.setMargins
import com.readystatesoftware.systembartint.SystemBarTintManager
import com.yoavst.changesystemohelshem.R
import kotlinx.android.synthetic.main.login_activity.*
import kotlinx.android.synthetic.main.login_view.*
import org.jetbrains.anko.browse
import org.jetbrains.anko.onClick
import org.jetbrains.anko.startActivity

class LoginActivity : MvpActivity<LoginView, LoginPresenter>(), LoginView {
    override var onDestroyCallback: (() -> Unit)? = null

    override fun createPresenter(): LoginPresenter = LoginPresenter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)
        initLayout()
        initViews()
        presenter.init()
    }

    override fun onDestroy() {
        super.onDestroy()
        onDestroyCallback?.invoke()
    }

    fun initLayout() {
        // add the padding from bottom to the layout
        if (Build.VERSION.SDK_INT >= 19) {
            val config = SystemBarTintManager(this).config
            if (Build.VERSION.SDK_INT >= 21) {
                if (!config.isNavigationAtBottom)
                    coordinatorLayout.setMargins(top = config.statusBarHeight)
                else
                    coordinatorLayout.setMargins(top = config.statusBarHeight, bottom = config.navigationBarHeight)
            } else {
                coordinatorLayout.setMargins(config.navigationBarHeight)
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
        layout.background = BitmapDrawable(resources, bitmap)
    }

    fun initViews() {
        registerFab.onClick {
            browse("http://ohel-shem.com/portal6/system/register.php")
        }
        loginButton.onClick { onLogin() }
        passwordInput.setOnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                onLogin()
                true
            }
            else false
        }
    }

    override fun showLoading() {
        passwordInputLayout.isEnabled = false
        idInputLayout.isEnabled = false
        hideKeyboard()
        progress.showNow()
    }

    override fun showLoginError(error: UpdateError) {
        progress.hideNow()
        passwordInputLayout.isEnabled = true
        idInputLayout.isEnabled = true
        when (error) {
            UpdateError.Connection -> Snackbar.make(coordinatorLayout, R.string.no_connection, Snackbar.LENGTH_SHORT).show()
            UpdateError.Login -> Snackbar.make(coordinatorLayout, R.string.login_wrong, Snackbar.LENGTH_SHORT).show()
            UpdateError.NoData -> throw IllegalStateException("Data should be returned from login")
            UpdateError.Exception -> Snackbar.make(coordinatorLayout, R.string.general_error, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun showIdInvalidError() {
        progress.hideNow()
        idInputLayout.error = null
        idInputLayout.error = getString(R.string.invalid_id)
    }

    override fun showPasswordInvalidError() {
        progress.hideNow()
        passwordInputLayout.error = null
        passwordInputLayout.error = getString(R.string.password_too_short)
    }

    override fun launchMainApp() {
        startActivity<MainActivity>()
        finish()
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