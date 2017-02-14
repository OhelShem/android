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

import com.hannesdorfmann.mosby.mvp.MvpView
import com.ohelshem.api.model.UpdateError
import com.ohelshem.app.controller.storage.Storage
import com.ohelshem.app.controller.storage.TeacherStorage

interface LoginView: MvpView {
    fun showLoading()

    fun showLoginError(error: UpdateError)
    fun showIdInvalidError()
    fun showPasswordInvalidError()

    fun launchApp()

    fun showTeachersDialog(teacherStorage: TeacherStorage, listener: () -> Unit)
    fun showStudentsDialog(studentStorage: Storage, listener: () -> Unit)
}