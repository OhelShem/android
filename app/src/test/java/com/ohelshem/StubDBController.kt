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

package com.ohelshem

import android.os.Message
import com.ohelshem.api.Role
import com.ohelshem.api.model.Change
import com.ohelshem.api.model.Hour
import com.ohelshem.api.model.Test
import com.ohelshem.api.model.UserData
import com.ohelshem.app.controller.DBController
import com.ohelshem.app.model.OverrideData
import com.ohelshem.app.model.Updatable
import java.io.File
import java.util.*

open class StubDBController : DBController {
    override var databaseVersion: Int = 0

    override var notificationsForChangesEnabled: Boolean = false

    override var notificationsForTestsEnabled: Boolean = false

    override var notificationsForHolidaysEnabled: Boolean = false

    override var notificationsForTimetableEnabled: Boolean = false

    override var guessingGameEnabled: Boolean = false

    override var lastNotificationTime: Long = 0

    override var developerModeEnabled: Boolean = false

    override var overrides: Array<OverrideData> = emptyArray()

    override var password: String = ""

    val overridesListeners: MutableMap<Int, Updatable<Array<OverrideData>>> = HashMap(1)

    override fun attachOverridesListener(id: Int, listener: Updatable<Array<OverrideData>>) {
        overridesListeners += id to listener
    }

    override fun removeOverrideListener(id: Int) {
        overridesListeners.keys -= id
    }

    val timetableListeners: MutableMap<Int, Updatable<Array<Array<Hour>>>> = HashMap(1)

    override fun attachTimetableListener(id: Int, listener: Updatable<Array<Array<Hour>>>) {
        timetableListeners += id to listener
    }

    override fun removeTimetableListener(id: Int) {
        timetableListeners.keys -= id
    }

    override fun bulk(changing: DBController.() -> Unit) {
    }

    override fun clearData() {
    }

    override fun migration() {
    }

    override fun init() {
    }

    override fun importOverrideFile(file: File) {
    }

    override fun exportOverrideFile(file: File): Boolean {
        return false
    }

    override var serverUpdateDate: Long = 0

    override var changesDate: Long = 0

    override var updateDate: Long = 0

    override var userData: UserData = UserData(0, "", "", "", 0, 0, 0, "", "", "", Role.Student) //can't leave the role parameter unspecified for some reason

    override var timetable: Array<Array<Hour>>? = null

    override var changes: List<Change>? = null

    override var tests: List<Test>? = null

    var messages: List<Message>? = null //removed override to get rid of the error

}