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

import com.ohelshem.api.model.Change
import com.ohelshem.api.model.Hour
import com.ohelshem.api.model.Test
import com.ohelshem.api.model.UserData

/*
 * A wrapper for [DBController] with support for caching for runtime.
 * The class that use [DBController] doesn't care if it receive the data from
 * runtime cache or from the database itself. It is known that every date that
 * is shown is a "cache", since it isn't live connected to the Ohel-Shem's service.
 */
class DBControllerWrapper(private val databaseController: DBController) : DBController by databaseController {
    private var _userData: UserData? = null
    override var userData: UserData
        get() {
            if (_userData == null)
                _userData = databaseController.userData
            return _userData!!
        }
        set(value) {
            _userData = value
            storeInDB { databaseController.userData = value }
        }

    private var _changesDate: Long? = null
    override var changesDate: Long
        get() {
            if (_changesDate == null)
                _changesDate = databaseController.changesDate
            return _changesDate!!
        }
        set(value) {
            _changesDate = value
            storeInDB { databaseController.changesDate = value }
        }

    private var _serverUpdateDate: Long? = null
    override var serverUpdateDate: Long
        get() {
            if (_serverUpdateDate == null)
                _serverUpdateDate = databaseController.serverUpdateDate
            return _serverUpdateDate!!
        }
        set(value) {
            _serverUpdateDate = value
            storeInDB { databaseController.serverUpdateDate = value }
        }

    private var _updateDate: Long? = null
    override var updateDate: Long
        get() {
            if (_updateDate == null)
                _updateDate = databaseController.updateDate
            return _updateDate!!
        }
        set(value) {
            _updateDate = value
            storeInDB { databaseController.updateDate = value }
        }

    private var _lastNotificationTime: Long? = null
    override var lastNotificationTime: Long
        get() {
            if (_lastNotificationTime == null)
                _lastNotificationTime = databaseController.lastNotificationTime
            return _lastNotificationTime!!
        }
        set(value) {
            _lastNotificationTime = value
            storeInDB { databaseController.lastNotificationTime = value }
        }

    private var _password: String? = null
    override var password: String
        get() {
            if (_password == null)
                _password = databaseController.password
            return _password!!
        }
        set(value) {
            _password = value
            storeInDB { databaseController.password = value }
        }

    private var _changes: List<Change>? = null
    override var changes: List<Change>?
        get() {
            if (_changes == null)
                _changes = databaseController.changes
            return _changes
        }
        set(value) {
            _changes = value?.let { if (it.isEmpty()) null else it }
            storeInDB { databaseController.changes = value }
        }

    private var _tests: List<Test>? = null
    override var tests: List<Test>?
        get() {
            if (_tests == null)
                _tests = databaseController.tests
            return _tests
        }
        set(value) {
            _tests = value
            storeInDB { databaseController.tests = value }
        }

    private var _timetable: Array<Array<Hour>>? = null
    override var timetable: Array<Array<Hour>>?
        get() {
            if (_timetable == null)
                _timetable = databaseController.timetable
            return _timetable
        }
        set(value) {
            _timetable = value
            storeInDB { databaseController.timetable = value }
        }

    private var _developerModeEnabled: Boolean? = null
    override var developerModeEnabled: Boolean
        get() {
            if (_developerModeEnabled == null)
                _developerModeEnabled = databaseController.developerModeEnabled
            return _developerModeEnabled!!
        }
        set(value) {
            _developerModeEnabled = value
            storeInDB { databaseController.developerModeEnabled = value }
        }

    private var bulkEnabled = false
    override fun bulk(changing: DBController.() -> Unit) {
        bulkEnabled = true
        changing()
        bulkEnabled = false
        databaseController.bulk(changing)
    }

    private inline fun storeInDB(operation: () -> Unit) {
        if (!bulkEnabled) operation()
    }
}