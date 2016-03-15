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

package com.ohelshem.app.android.tests

import com.hannesdorfmann.mosby.mvp.MvpBasePresenter
import com.ohelshem.api.model.UpdateError
import com.ohelshem.app.android.base.presenter.InitializerPresenter
import com.ohelshem.app.controller.ApiController
import com.ohelshem.app.controller.ApiController.Api
import com.ohelshem.app.controller.DBController
import uy.kohesive.injekt.injectLazy

class TestsPresenter : MvpBasePresenter<TestsView>(), InitializerPresenter, ApiController.Callback {
    private val databaseController: DBController by injectLazy()

    override fun init() {
        load()
    }

    fun load() {
        view?.update(databaseController.tests!!)
    }

    override fun onSuccess(apis: List<ApiController.Api>) {
        if (Api.Tests in apis)
            view?.update(databaseController.tests!!)
    }

    override fun onFail(error: UpdateError) {
        // Ignored
    }
}