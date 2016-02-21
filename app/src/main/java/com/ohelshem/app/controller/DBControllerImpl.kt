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

import com.chibatching.kotpref.Kotpref
import com.chibatching.kotpref.KotprefModel
import com.ohelshem.app.model.OverrideData
import com.ohelshem.app.model.Updatable
import com.ohelshem.app.controller.ApiDatabase
import com.ohelshem.api.model.*
import uy.kohesive.injekt.injectLazy
import java.io.File
import java.util.*

/**
 * The default [DBController]. Use [KotprefModel] for store the key-value data.
 * It store the changes as another file for better performance.
 */
object DBControllerImpl : KotprefModel(), DBController {
    private val offsetDataController: OffsetDataController by injectLazy()

    override var databaseVersion: Int by intPrefVar(3)
    override var notificationsForChangesEnabled: Boolean by booleanPrefVar()
    override var notificationsForHolidaysEnabled: Boolean by booleanPrefVar()
    override var notificationsForTestsEnabled: Boolean by booleanPrefVar()
    override var notificationsForTimetableEnabled: Boolean by booleanPrefVar()
    override var guessingGameEnabled: Boolean by booleanPrefVar(true)
    override var lastNotificationTime: Long by longPrefVar()
    override var developerModeEnabled: Boolean by booleanPrefVar()

    private val overridesListeners: MutableMap<Int, Updatable<Array<OverrideData>>> = HashMap(1)

    override fun attachOverridesListener(id: Int, listener: Updatable<Array<OverrideData>>) {
        overridesListeners += id to listener
    }

    override fun removeOverrideListener(id: Int) {
        overridesListeners.keys -= id
    }

    private val timetableListeners: MutableMap<Int, Updatable<Array<Array<Hour>>>> = HashMap(1)

    override fun attachTimetableListener(id: Int, listener: Updatable<Array<Array<Hour>>>) {
        timetableListeners += id to listener
    }

    override fun removeTimetableListener(id: Int) {
        timetableListeners.keys -= id
    }

    override var changesDate: Long by longPrefVar(DBController.EmptyData.toLong())
    override var serverUpdateDate: Long by longPrefVar(ApiDatabase.EmptyUpdateDate)
    override var updateDate: Long by longPrefVar(ApiDatabase.EmptyUpdateDate)


    override var password: String by stringPrefVar()

    override var timetable: Array<Array<Hour>>?
        get() {
            if (!TimetableDataFile.exists() || !TimetableOffsetFile.exists()) return null
            val data = offsetDataController.read(TimetableOffsetFile, TimetableDataFile, OffsetDataController.AllFile)
            return Array(data.size / 10) { day ->
                val offset = day * 10
                Array(10) { hour ->
                    data[offset + hour].split(InnerSeparator, limit = 3).let { Hour(it[0], it[1], it[2].toInt()) }
                }
            }
        }
        set(value) {
            if (value == null || value.isEmpty()) {
                TimetableDataFile.delete()
                TimetableOffsetFile.delete()
                timetableListeners.values.forEach { it.onUpdate(emptyArray()) }
            } else {
                init()
                value.flatMap { it.toList() }.map { it.name + InnerSeparator + it.teacher + InnerSeparator + it.color.toString() }.let {
                    offsetDataController.write(TimetableOffsetFile, TimetableDataFile, it)
                }
                timetableListeners.values.forEach { it.onUpdate(value) }
            }
        }


    override var changes: List<Change>?
        get() {
            if (!ChangesDataFile.exists() || !ChangesOffsetFile.exists()) return null
            val data = offsetDataController.read(ChangesOffsetFile, ChangesDataFile, OffsetDataController.AllFile)
            val list = ArrayList<Change>(data.size)
            for (i in 0 until data.size) {
                list += data[i].split(InnerSeparator, limit = 4).let { Change(it[0].toInt(), it[1].toInt(), it[2], it[3].toInt()) }
            }
            return list
        }
        set(value) {
            if (value == null || value.isEmpty()) {
                ChangesDataFile.delete()
                ChangesOffsetFile.delete()
            } else {
                init()
                value.map { it.clazz.toString() + InnerSeparator + it.hour.toString() + InnerSeparator + it.content + InnerSeparator + it.color.toString() }.let {
                    offsetDataController.write(ChangesOffsetFile, ChangesDataFile, it)
                }
            }
        }

    override var tests: List<Test>?
        get() {
            if (!TestsDataFile.exists() || !TestsOffsetFile.exists()) return null
            val data = offsetDataController.read(TestsOffsetFile, TestsDataFile, OffsetDataController.AllFile)
            val list = ArrayList<Test>(data.size)
            for (i in 0 until data.size) {
                list += data[i].split(InnerSeparator, limit = 3).let { Test(it[0].toLong(), it[1]) }
            }
            return list
        }
        set(value) {
            if (value == null) {
                TestsDataFile.delete()
                TestsOffsetFile.delete()
            } else {
                init()
                value.map { it.date.toString() + InnerSeparator + it.content.toString() }.let {
                    offsetDataController.write(TestsOffsetFile, TestsDataFile, it)
                }
            }
        }

    override var messages: List<Message>?
        get() {
            if (!MessagesDataFile.exists() || !MessagesOffsetFile.exists()) return null
            val data = offsetDataController.read(MessagesOffsetFile, MessagesDataFile, OffsetDataController.AllFile)
            val list = ArrayList<Message>(data.size)
            for (i in 0 until data.size) {
                list += data[i].split(InnerSeparator, limit = 5).let {
                    Message(it[0].toInt(), it[1], it[2], it[3].toInt(), it[4].toLong())
                }
            }
            return list
        }
        set(value) {
            if (value == null) {
                TestsDataFile.delete()
                TestsOffsetFile.delete()
            } else {
                init()
                value.map { it.id.toString() + InnerSeparator + it.title + InnerSeparator + it.content + InnerSeparator + it.author + InnerSeparator + it.date }.let {
                    offsetDataController.write(MessagesOffsetFile, MessagesDataFile, it)
                }
            }
        }

    override var userData: UserData
        get() = UserData(_userId, _userIdentity, _userPrivateName, _userFamilyName, _userLayer, _userClazz, _userGender, _userEmail, _userPhone, _userBirthday)
        set(value) {
            _userId = value.id
            _userIdentity = value.identity
            _userPrivateName = value.privateName
            _userFamilyName = value.familyName
            _userLayer = value.layer
            _userClazz = value.clazz
            _userGender = value.gender
            _userEmail = value.email
            _userPhone = value.phone
            _userBirthday = value.birthday
        }

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

    private val InnerSeparator: Char = '\u2004'

    //region Overrides
    override var overrides: Array<OverrideData>
        get() {
            try {
                return readOverrides()
            } catch(e: Exception) {
                TimetableOverridesFile.delete()
                return emptyArray()
            }
        }
        set(value) {
            setOverridesSilently(value)
            overridesListeners.values.forEach { it.onUpdate(value) }
        }


    private fun readOverrides(): Array<OverrideData> {
        if (!TimetableOverridesFile.exists()) return emptyArray()
        val data = TimetableOverridesFile.readLines()
        return Array(data.size) { i ->
            data[i].split('^').let { OverrideData(it[0].toInt(), it[1].toInt(), it[2], it[3]) }
        }
    }

    private fun setOverridesSilently(value: Array<OverrideData>) {
        init()
        TimetableOverridesFile.createNewFile()
        TimetableOverridesFile.writeText("")
        value.forEach { TimetableOverridesFile.appendText("${it.day}^${it.hour}^${it.newName}^${it.newTeacher}\n") }
    }

    fun readOverridesOldWay(): Array<OverrideData> {
        if (!TimetableOverridesFile.exists()) return emptyArray()
        val data = TimetableOverridesFile.readLines()
        try {
            return Array(data.size) { i ->
                data[i].split(',').let { OverrideData(it[0].toInt(), it[1].toInt(), it[2], "") }
            }
        } catch(e: Exception) {
            TimetableOverridesFile.delete()
            return emptyArray()
        }
    }

    override fun importOverrideFile(file: File) {
        init()
        if (TimetableOverridesFile.exists()) {
            TimetableOverridesFileBackup.createNewFile()
            TimetableOverridesFile.copyTo(TimetableOverridesFileBackup, overwrite = true)
        } else TimetableOverridesFile.createNewFile()
        file.copyTo(TimetableOverridesFile, overwrite = true)
        var data: Array<OverrideData>
        try {
            data = readOverrides()
        } catch (e: Exception) {
            try {
                data = readOverridesOldWay()
                setOverridesSilently(data)
            } catch (e: Exception) {
                if (TimetableOverridesFileBackup.exists())
                    TimetableOverridesFileBackup.copyTo(TimetableOverridesFile, overwrite = true)
                throw IllegalArgumentException()
            }
        } finally {
            TimetableOverridesFileBackup.delete()
        }
        overridesListeners.values.forEach { it.onUpdate(data) }
    }

    override fun exportOverrideFile(file: File): Boolean {
        if (TimetableOverridesFile.exists()) {
            try {
                init()
                TimetableOverridesFile.copyTo(file, overwrite = true)
                return true
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
        } else return false
    }
    //endregion

    override fun migration() {
        if (databaseVersion == 0) {
            // New format for changes, now not wrapping hours.
            ChangesDataFile.delete()
            ChangesOffsetFile.delete()
            databaseVersion = 1
        }
        if (databaseVersion == 1) {
            // New format for overrides
            if (TimetableOverridesFileOld.exists()) {
                TimetableOverridesFile.createNewFile()
                TimetableOverridesFileOld.copyTo(TimetableOverridesFile, true)
            }
            val old = readOverridesOldWay()
            setOverridesSilently(old)
            databaseVersion = 2
        }
        if (databaseVersion == 2) {
            // Moved to new api
            clearData()
            databaseVersion = 3
        }
    }

    override fun bulk(changing: DBController.() -> Unit) = Kotpref.bulk(DBControllerImpl, changing)

    override fun clearData() {
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


    internal val ChangesDataFile = File("/data/data/com.yoavst.changesystemohelshem/files/changes3.bin")
    internal val ChangesOffsetFile = File("/data/data/com.yoavst.changesystemohelshem/files/changes3_offsets.bin")

    internal val TestsDataFile = File("/data/data/com.yoavst.changesystemohelshem/files/tests3.bin")
    internal val TestsOffsetFile = File("/data/data/com.yoavst.changesystemohelshem/files/tests3_offsets.bin")

    internal val MessagesDataFile = File("/data/data/com.yoavst.changesystemohelshem/files/messages3.bin")
    internal val MessagesOffsetFile = File("/data/data/com.yoavst.changesystemohelshem/files/messages3_offsets.bin")

    internal val TimetableDataFile = File("/data/data/com.yoavst.changesystemohelshem/files/timetable3.bin")
    internal val TimetableOffsetFile = File("/data/data/com.yoavst.changesystemohelshem/files/timetable3_offsets.bin")

    internal val TimetableOverridesFile = File("/data/data/com.yoavst.changesystemohelshem/files/timetable_overrides.csv")
    internal val TimetableOverridesFileOld = File("/data/data/com.yoavst.changesystemohelshem/files/timetable_overrides.cvs")
    internal val TimetableOverridesFileBackup = File("/data/data/com.yoavst.changesystemohelshem/files/timetable_overrides_backup.csv")
    internal val FilesFolder = File("/data/data/com.yoavst.changesystemohelshem/files/")

    override fun init() {
        if (!FilesFolder.exists())
            FilesFolder.mkdir()
    }

}