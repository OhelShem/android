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

package com.ohelshem.app.android.login

import com.ohelshem.api.model.UpdateError
import com.ohelshem.app.android.utils.BasePresenter
import com.ohelshem.app.controller.analytics.Analytics
import com.ohelshem.app.controller.api.ApiController
import com.ohelshem.app.controller.storage.SharedStorage

class LoginPresenter(private val storage: SharedStorage, private val apiController: ApiController, val analytics: Analytics) : BasePresenter<LoginView>(), ApiController.Callback {
    var lastPassword: String = ""
    var lastId: String = ""


    override fun onCreate() {
        apiController[Id] = this
    }

    override fun onDestroy() {
        apiController -= Id

        lastPassword = ""
        lastId = ""
    }


    fun login(id: String, password: String) {
        if (!apiController.isBusy) {
            if (id.length != 9 || !id.all(Char::isDigit)) view?.showIdInvalidError()
            else if (password.length < 4) view?.showPasswordInvalidError()
            else {
                lastPassword = password
                lastId = id
                apiController.setAuthData(id, password)
                apiController.login()
                view?.showLoading()
            }
        }
    }

    override fun onSuccess(apis: Set<ApiController.UpdatedApi>) {
        storage.password = lastPassword
        storage.id = lastId
        analytics.onLogin()
        view?.launchApp()

        onDestroy()
    }

    override fun onFail(error: UpdateError) {
        view?.showLoginError(error)
    }

    companion object {
        private const val Id = 532
    }
}