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

package com.ohelshem.app.controller

import com.ohelshem.app.model.Updatable
import com.ohelshem.api.model.Hour
import com.ohelshem.api.model.UpdateError
import uy.kohesive.injekt.injectLazy

/**
 * The default [TimetableController].
 */
class TimetableControllerImpl : BaseTimetableController(), Updatable<Array<Array<Hour>>> {
    private val databaseController: DBController by injectLazy()

    override fun init() {
        databaseController.attachTimetableListener(1, this)
        databaseController.timetable?.let { onUpdate(it) }
    }

    override fun onUpdate(data: Array<Array<Hour>>) {
        timetable = data
    }

    override fun onError(error: UpdateError) {
        // Ignored. Keep the current timetable.
    }

}