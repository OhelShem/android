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

import com.hannesdorfmann.mosby.mvp.MvpBasePresenter
import com.ohelshem.api.model.AuthData
import com.ohelshem.api.model.UpdateError
import com.ohelshem.app.android.base.presenter.InitializerPresenter
import com.ohelshem.app.controller.ApiController
import com.ohelshem.app.controller.DBController
import uy.kohesive.injekt.injectLazy

class LoginPresenter : MvpBasePresenter<LoginView>(), InitializerPresenter, ApiController.Callback {
    val apiController: ApiController by injectLazy()
    val databaseController: DBController by injectLazy()

    var lastPassword: String = ""


    override fun init() {
        apiController[Id] = this
        apiController.authData = AuthData("", "")
        view?.onDestroyCallback = { apiController -= Id }
    }

    fun login(id: String, password: String) {
        if (!apiController.isBusy) {
            if (id.length != 9 || !id.all(Char::isDigit)) view?.showIdInvalidError()
            else if (password.length < 4) view?.showPasswordInvalidError()
            else {
                lastPassword = password
                apiController.authData = AuthData(id, password)
                apiController.login()
                view?.showLoading()
            }
        }
    }

    override fun onSuccess(apis: List<ApiController.Api>) {
        databaseController.password = lastPassword
        view?.launchMainApp()
    }

    override fun onFail(error: UpdateError) {
        view?.showLoginError(error)
    }

    companion object {
        private const val Id = 532
    }


}