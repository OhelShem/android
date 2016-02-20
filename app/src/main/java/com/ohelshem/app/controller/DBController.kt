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

import com.ohelshem.app.model.OverrideData
import com.ohelshem.app.model.Updatable
import com.ohelshem.api.controller.declaration.ApiDatabase
import com.ohelshem.api.model.Hour
import com.ohelshem.app.*
import java.io.File

/**
 * An interface for classes that control the application's data storage.
 */
interface DBController : ApiDatabase {
    var databaseVersion: Int

    var notificationsForChangesEnabled: Boolean
    var notificationsForTestsEnabled: Boolean
    var notificationsForHolidaysEnabled: Boolean
    var notificationsForTimetableEnabled: Boolean
    var guessingGameEnabled: Boolean

    var lastNotificationTime: Long

    var developerModeEnabled: Boolean

    var overrides: Array<OverrideData>

    var password: String

    fun attachOverridesListener(id: Int, listener: Updatable<Array<OverrideData>>)
    fun removeOverrideListener(id: Int)
    fun attachTimetableListener(id: Int, listener: Updatable<Array<Array<Hour>>>)
    fun removeTimetableListener(id: Int)

    fun bulk(changing: DBController.() -> Unit)
    fun clearData()

    fun migration()

    fun init()

    fun isSetup(): Boolean = !userData.isEmpty() && timetable != null

    fun isCacheUpdated(): Boolean {
        val cal = changesDate.toCalendar().clearTime()
        return cal.isToday() && getHour() < 21 || cal.isTomorrow()
    }

    fun hasChanges(): Boolean = isSetup() && changes?.let { it.size != 0 && it.firstOrNull { it.clazz == userData.clazz } != null } ?: false

    fun importOverrideFile(file: File)
    fun exportOverrideFile(file: File): Boolean

    companion object {
        fun classesAtLayer(layer: Int): Int {
            return when (layer) {
                9, 10, 11 -> 12
                12 -> 11
                else -> throw IllegalArgumentException()
            }
        }

        const val EmptyData = 0
    }
}