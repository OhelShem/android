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

package com.ohelshem.app.controller.storage.implementation

import com.chibatching.kotpref.KotprefModel
import com.ohelshem.api.Role
import com.ohelshem.api.controller.implementation.ApiParserImpl
import com.ohelshem.api.model.Change
import com.ohelshem.api.model.Hour
import com.ohelshem.api.model.Test
import com.ohelshem.api.model.UserData
import com.ohelshem.app.controller.utils.OffsetDataController
import com.ohelshem.app.controller.utils.OffsetDataControllerImpl
import com.ohelshem.app.model.OverrideData
import java.io.File
import java.util.*

object OldStorage : KotprefModel() {
    private val offsetDataController: OffsetDataController = OffsetDataControllerImpl
    override val kotprefName: String = "DBControllerImpl"

    var databaseVersion: Int by intPrefVar(3)
    var notificationsForChangesEnabled: Boolean by booleanPrefVar()
    var notificationsForHolidaysEnabled: Boolean by booleanPrefVar()
    var notificationsForTestsEnabled: Boolean by booleanPrefVar()
    var notificationsForTimetableEnabled: Boolean by booleanPrefVar()
    var developerModeEnabled: Boolean by booleanPrefVar()

    var password: String by stringPrefVar()

    val timetable: Array<Array<Hour>>?
        get() {
            if (!TimetableDataFile.exists() || !TimetableOffsetFile.exists()) return null
            try {
                val data = offsetDataController.read(TimetableOffsetFile, TimetableDataFile, OffsetDataController.AllFile)
                val hours = ApiParserImpl.MaxHoursADay
                return Array(data.size / hours) { day ->
                    val offset = day * hours
                    Array(hours) { hour ->
                        data[offset + hour].split(InnerSeparator, limit = 3).let { Hour(it[0], it[1], it[2].toInt()) }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }


    val changes: List<Change>?
        get() {
            if (!ChangesDataFile.exists() || !ChangesOffsetFile.exists()) return null
            val data = offsetDataController.read(ChangesOffsetFile, ChangesDataFile, OffsetDataController.AllFile)
            val list = ArrayList<Change>(data.size)
            for (i in 0 until data.size) {
                list += data[i].split(InnerSeparator, limit = 4).let { Change(it[0].toInt(), it[1].toInt(), it[2], it[3].toInt()) }
            }
            return list
        }


    val tests: List<Test>?
        get() {
            if (!TestsDataFile.exists() || !TestsOffsetFile.exists()) return null
            val data = offsetDataController.read(TestsOffsetFile, TestsDataFile, OffsetDataController.AllFile)
            val list = ArrayList<Test>(data.size)
            for (i in 0 until data.size) {
                list += data[i].split(InnerSeparator, limit = 3).let { Test(it[0].toLong(), it[1]) }
            }
            return list
        }


    val userData: UserData
        get() = UserData(_userId, _userIdentity, _userPrivateName, _userFamilyName, _userLayer, _userClazz, _userGender, _userEmail, _userPhone, _userBirthday, Role.values()[_userRole])

    private var _userClazz: Int by intPrefVar()
    private var _userLayer: Int by intPrefVar()
    private var _userId: Int by intPrefVar()
    private var _userIdentity: String by stringPrefVar()
    private var _userPrivateName: String by stringPrefVar()
    private var _userFamilyName: String by stringPrefVar()
    private var _userGender: Int by intPrefVar()
    private var _userEmail: String by stringPrefVar()
    private var _userPhone: String by stringPrefVar()
    private var _userBirthday: String by stringPrefVar()
    private var _userRole: Int by intPrefVar()

    private val InnerSeparator: Char = '\u2004'

    val overrides: Array<OverrideData>
        get() {
            try {
                return readOverrides()
            } catch(e: Exception) {
                TimetableOverridesFile.delete()
                return emptyArray()
            }
        }


    private fun readOverrides(): Array<OverrideData> {
        if (!TimetableOverridesFile.exists()) return emptyArray()
        val data = TimetableOverridesFile.readLines()
        return Array(data.size) { i ->
            data[i].split('^').let { OverrideData(it[0].toInt(), it[1].toInt(), it[2], it[3]) }
        }
    }

    fun clearData() {
        clear()
        ChangesDataFile.delete()
        ChangesOffsetFile.delete()
        TimetableDataFile.delete()
        TimetableOffsetFile.delete()
        TestsDataFile.delete()
        TestsOffsetFile.delete()
        MessagesDataFile.delete()
        MessagesOffsetFile.delete()
        TimetableOverridesFile.delete()
    }


    private val ChangesDataFile by lazy { File(FilesFolder, "changes3.bin") }
    private val ChangesOffsetFile by lazy { File(FilesFolder, "changes3_offsets.bin") }
    private val TestsDataFile by lazy { File(FilesFolder, "tests3.bin") }
    private val TestsOffsetFile by lazy { File(FilesFolder, "tests3_offsets.bin") }
    private val MessagesDataFile by lazy { File(FilesFolder, "messages3.bin") }
    private val MessagesOffsetFile by lazy { File(FilesFolder, "messages3_offsets.bin") }
    private val TimetableDataFile by lazy { File(FilesFolder, "timetable3.bin") }
    private val TimetableOffsetFile by lazy { File(FilesFolder, "timetable3_offsets.bin") }
    private val TimetableOverridesFile by lazy { File(FilesFolder, "timetable_overrides.csv") }

    private val FilesFolder: File by lazy { context.filesDir }
}