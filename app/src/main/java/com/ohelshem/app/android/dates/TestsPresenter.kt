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

package com.ohelshem.app.android.dates

import com.ohelshem.api.model.UpdateError
import com.ohelshem.app.android.utils.BasePresenter
import com.ohelshem.app.controller.api.ApiController
import com.ohelshem.app.controller.api.ApiController.UpdatedApi
import com.ohelshem.app.controller.storage.Storage

class TestsPresenter(val storage: Storage) : BasePresenter<TestsView>(), ApiController.Callback {
    override fun onSuccess(apis: Set<UpdatedApi>) {
        if (UpdatedApi.Tests in apis)
            load()
    }

    override fun onCreate() {
        load()
    }

    private fun load() {
        view?.update(storage.tests ?: emptyList())
    }

    override fun onDestroy() = Unit // ignored

    override fun onFail(error: UpdateError) = Unit // ignored
}