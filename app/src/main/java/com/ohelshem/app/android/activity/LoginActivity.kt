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

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import com.readystatesoftware.systembartint.SystemBarTintManager
import com.yoavst.changesystemohelshem.R
import com.ohelshem.app.android.hideKeyboard
import com.ohelshem.app.android.setMargins
import com.ohelshem.app.controller.DBController
import com.ohelshem.app.controller.ApiController
import com.ohelshem.api.model.AuthData
import com.ohelshem.api.model.UpdateError
import kotlinx.android.synthetic.main.login_activity.*
import kotlinx.android.synthetic.main.login_view.*
import org.jetbrains.anko.browse
import org.jetbrains.anko.onClick
import org.jetbrains.anko.startActivity
import uy.kohesive.injekt.injectLazy

class LoginActivity : AppCompatActivity(), ApiController.Callback {
    val apiController: ApiController by injectLazy()
    val databaseController: DBController by injectLazy()

    var lastPassword: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)
        initLayout()
        initViews()
        initFunctionality()
    }

    override fun onDestroy() {
        super.onDestroy()
        apiController -= Id
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
    }

    fun initFunctionality() {
        apiController[Id] = this
        apiController.authData = AuthData("", "")
    }


    fun onLogin() {
        if (!apiController.isBusy) {
            idInputLayout.error = null
            passwordInputLayout.error = null
            val id = idInputLayout.editText!!.text.toString()
            val password = passwordInputLayout.editText!!.text.toString()
            if (id.length != 9 || !id.all(Char::isDigit)) idInputLayout.error = getString(R.string.invalid_id)
            else if (password.length < 4) passwordInputLayout.error = getString(R.string.password_too_short)
            else {
                lastPassword = password
                passwordInputLayout.isEnabled = false
                idInputLayout.isEnabled = false
                apiController.authData = AuthData(id, password)
                apiController.login()
                hideKeyboard()
                progress.showNow()
            }
        }
    }

    override fun onSuccess(apis: List<ApiController.Api>) {
        databaseController.password = lastPassword
        startActivity<MainActivity>()
        finish()
    }

    override fun onFail(error: UpdateError) {
        progress.hideNow()
        passwordInputLayout.isEnabled = true
        idInputLayout.isEnabled = true
        when (error) {
            UpdateError.Connection -> Snackbar.make(coordinatorLayout, R.string.no_connection, Snackbar.LENGTH_SHORT).show()
            UpdateError.Login -> Snackbar.make(coordinatorLayout, R.string.login_wrong, Snackbar.LENGTH_SHORT).show()
            UpdateError.NoData -> throw IllegalStateException("Data should be returned from login")
            UpdateError.Exception -> Snackbar.make(coordinatorLayout, R.string.general_error, Snackbar.LENGTH_SHORT).show()
        }
        if (error == UpdateError.Connection) {
            Snackbar.make(coordinatorLayout, R.string.no_connection, Snackbar.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val Id = 532
    }
}