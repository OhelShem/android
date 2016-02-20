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

package com.ohelshem.app.model

import android.widget.Spinner
/**
 * Classes which inherit from this interface give their fragments a better control.
 */
interface DrawerActivity {
    /**
     * Set the main fragment to the new Fragment.
     * If [replace] is false, it will be added to the stack.
     */
    fun setFragment(fragmentType: FragmentType, backStack: Boolean = false)

    fun openDrawer()

    fun refresh()

    fun getNavigationSpinner(): Spinner

    fun setToolbarTitle(title: String)

    companion object {
        enum class FragmentType {
            Dashboard,
            Changes,
            Timetable,
            Tests,
            LayerChanges,
            Holidays
        }
    }
}