package com.ohelshem.app.controller.storage.implementation

import android.support.v7.app.AppCompatDelegate
import com.chibatching.kotpref.KotprefModel
import com.ohelshem.api.Role
import com.ohelshem.api.controller.implementation.ApiParserImpl
import com.ohelshem.api.model.Hour
import com.ohelshem.api.model.UserData
import com.ohelshem.app.controller.serialization.HourSerialization
import com.ohelshem.app.controller.serialization.ofList
import com.ohelshem.app.controller.serialization.simpleReader
import com.ohelshem.app.controller.serialization.simpleWriter
import com.ohelshem.app.controller.storage.IStorage.Companion.EmptyData
import com.ohelshem.app.controller.storage.SharedStorage
import com.ohelshem.app.controller.storage.SharedStorage.Theme
import com.ohelshem.app.controller.utils.OffsetDataController
import com.ohelshem.app.model.OverrideData
import com.yoavst.changesystemohelshem.BuildConfig
import mu.KLogging
import java.io.File
import java.util.*

class SharedStorageImpl(private val offsetDataController: OffsetDataController) : SharedStorage, KotprefModel() {
    override var version: Int by intPrefVar(EmptyData)

    override var id: String by stringPrefVar()
    override var password: String by stringPrefVar()

    override var notificationsForChanges: Boolean by booleanPrefVar()
    override var notificationsForHolidays: Boolean by booleanPrefVar()
    override var notificationsForBirthdays: Boolean by booleanPrefVar()
    override var notificationsForTests: Boolean by booleanPrefVar()
    override var notificationsForTimetable: Boolean by booleanPrefVar()

    override var developerMode: Boolean by booleanPrefVar()

    override var changesDate: Long by longPrefVar(EmptyData.toLong())
    override var serverUpdateDate: Long by longPrefVar(EmptyData.toLong())
    override var updateDate: Long by longPrefVar(EmptyData.toLong())
    override var lastNotificationTime: Long by longPrefVar(EmptyData.toLong())

    override var appVersion: Int by intPrefVar(EmptyData)
    override var debugFlag: Boolean by booleanPrefVar()

    var _theme: Int by intPrefVar(Theme.Blue.ordinal)
    override var theme: Theme
        get() = Theme.values()[_theme]
        set(value) {
            _theme = value.ordinal
        }

    override var darkMode: Int by intPrefVar(AppCompatDelegate.MODE_NIGHT_AUTO)

    //region UserData
    override var userData: UserData
        get() = UserData(_userId, id, _userPrivateName, _userFamilyName, _userLayer, _userClazz, _userGender, _userEmail, _userPhone, _userBirthday, Role.values()[_userRole])
        set(value) {
            id = value.identity
            _userId = value.id
            _userPrivateName = value.privateName
            _userFamilyName = value.familyName
            _userLayer = value.layer
            _userClazz = value.clazz
            _userGender = value.gender
            _userEmail = value.email
            _userPhone = value.phone
            _userBirthday = value.birthday
            _userRole = value.role.ordinal
        }

    private var _userClazz: Int by intPrefVar()
    private var _userLayer: Int by intPrefVar()
    private var _userId: Int by intPrefVar()
    private var _userPrivateName: String by stringPrefVar()
    private var _userFamilyName: String by stringPrefVar()
    private var _userGender: Int by intPrefVar()
    private var _userEmail: String by stringPrefVar()
    private var _userPhone: String by stringPrefVar()
    private var _userBirthday: String by stringPrefVar()
    private var _userRole: Int by intPrefVar()
    //endregion

    //region Timetable
    private val timetableListeners: MutableMap<Int, (Array<Array<Hour>>) -> Unit> = HashMap(1)

    override fun attachTimetableListener(id: Int, listener: (Array<Array<Hour>>) -> Unit) {
        timetableListeners[id] = listener
    }

    override fun removeTimetableListener(id: Int) {
        timetableListeners.keys -= id
    }

    private val hourSerialization = HourSerialization.ofList()
    override var timetable: Array<Array<Hour>>?
        get() {
            if (!TimetableDataFile.exists()) return null
            try {
                TimetableDataFile.simpleReader().use { reader ->
                    val data = hourSerialization.deserialize(reader)
                    val hours = ApiParserImpl.MaxHoursADay
                    return Array(data.size / hours) { day ->
                        val offset = day * hours
                        Array(hours) { hour ->
                            data[offset + hour]
                        }
                    }
                }
            } catch (e: Exception) {
                logger.error(e) { "Reading timetable failed" }
                return null
            }
        }
        set(value) {
            if (value == null || value.isEmpty()) {
                TimetableDataFile.delete()
                timetableListeners.values.forEach { it(emptyArray()) }
            } else {
                prepare()
                TimetableDataFile.simpleWriter().use { writer ->
                    hourSerialization.serialize(writer, value.flatMap { it.toList() })
                }
                timetableListeners.values.forEach { it(value) }
            }
        }
    //endregion

    //region Overrides
    private val overridesListeners: MutableMap<Int, (List<OverrideData>) -> Unit> = HashMap(1)

    override fun attachOverridesListener(id: Int, listener: (List<OverrideData>) -> Unit) {
        overridesListeners[id] = listener
    }

    override fun removeOverrideListener(id: Int) {
        overridesListeners.keys -= id
    }

    override var overrides: List<OverrideData>
        get() {
            try {
                return Overrides.read(LatestVersion, TimetableOverridesFile)
            } catch(e: Exception) {
                e.printStackTrace()
                TimetableOverridesFile.delete()
                return emptyList()
            }
        }
        set(value) {
            setOverridesSilently(value)
            overridesListeners.values.forEach { it(value) }
        }

    private fun setOverridesSilently(value: List<OverrideData>) {
        prepare()
        TimetableOverridesFile.createNewFile()
        Overrides.write(TimetableOverridesFile, value)
    }

    override fun importOverrideFile(file: File) {
        prepare()
        if (TimetableOverridesFile.exists()) {
            TimetableOverridesFileBackup.createNewFile()
            TimetableOverridesFile.copyTo(TimetableOverridesFileBackup, overwrite = true)
        } else TimetableOverridesFile.createNewFile()
        file.copyTo(TimetableOverridesFile, overwrite = true)

        var data: List<OverrideData>? = null
        for (version in Overrides.Versions) {
            try {
                data = Overrides.read(version, TimetableOverridesFile)
                break
            } catch (e: Exception) {
            }
        }
        if (data != null) {
            setOverridesSilently(data)
            for (listener in overridesListeners.values) {
                listener(data)
            }
        } else {
            if (TimetableOverridesFileBackup.exists())
                TimetableOverridesFileBackup.copyTo(TimetableOverridesFileV4, overwrite = true)
        }
        TimetableOverridesFileBackup.delete()
    }

    override fun exportOverrideFile(file: File): Boolean {
        if (TimetableOverridesFile.exists()) {
            try {
                prepare()
                TimetableOverridesFile.copyTo(file, overwrite = true)
                return true
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
        } else return false
    }
    //endregion

    override fun clean() {
        clear()
        version = LatestVersion
        appVersion = BuildConfig.VERSION_CODE
        Files.forEach { it.delete() }
    }

    override fun migration() {
        if (version == EmptyData) {
            val old = OldStorage
            if (old.databaseVersion != 3) {
                old.clearData()
                FilesFolder.deleteRecursively()
                FilesFolder.mkdir()
            } else {
                val oldId = old.userData.identity
                val oldPassword = old.password
                val oldTimetable = old.timetable
                if (oldId.isNotEmpty() && oldPassword.isNotEmpty() && oldTimetable != null) {
                    id = oldId
                    password = oldPassword
                    userData = old.userData

                    developerMode = old.developerModeEnabled

                    notificationsForChanges = old.notificationsForChangesEnabled

                    notificationsForHolidays = old.notificationsForHolidaysEnabled

                    notificationsForTests = old.notificationsForTestsEnabled

                    notificationsForTimetable = old.notificationsForTimetableEnabled

                    updateDate = 0
                    serverUpdateDate = 0

                    timetable = oldTimetable
                    overrides = old.overrides.toList()
                }
            }
            old.clearData()
        }
        if (version == 4) {
            // Migrate timetable
            if (TimetableDataFileV4.exists() && TimetableOffsetFileV4.exists()) {
                try {
                    val data = offsetDataController.read(TimetableOffsetFileV4, TimetableDataFileV4, OffsetDataController.AllFile)
                    val hours = ApiParserImpl.MaxHoursADay
                    val timetable = Array(data.size / hours) { day ->
                        val offset = day * hours
                        Array(hours) { hour ->
                            data[offset + hour].split(InnerSeparator, limit = 3).let { Hour(it[0], it[1], it[2].toInt()) }
                        }
                    }
                    this.timetable = timetable
                    TimetableOffsetFileV4.delete()
                    TimetableDataFileV4.delete()
                } catch (e: Exception) {
                }
            }
            // Migrate overrides
            if (TimetableOverridesFileV4.exists()) {
                val data = TimetableOverridesFileV4.readLines()
                val overrides = (0 until data.size).map { i ->
                    data[i].split('^').let { OverrideData(it[0].toInt(), it[1].toInt(), it[2], it[3]) }
                }
                this.overrides = overrides
                TimetableOverridesFileV4.delete()
            }
        }
        version = LatestVersion
    }

    override fun prepare() {
        if (!FilesFolder.exists())
            FilesFolder.mkdir()
    }

    private val Files: Array<File> by lazy { arrayOf(TimetableDataFile, TimetableDataFile, TimetableOverridesFile, TimetableOverridesFileBackup) }

    private val FilesFolder: File by lazy { context.filesDir }

    private val TimetableDataFile: File by lazy { File(FilesFolder, "timetable5.bin") }

    private val TimetableOverridesFile: File by lazy { File(FilesFolder, "timetable5_overrides.bin") }
    private val TimetableOverridesFileBackup: File by lazy { File(FilesFolder, "timetable5_overrides_backup.bin") }

    //region compatibility
    private val TimetableDataFileV4: File by lazy { File(FilesFolder, "timetable4.bin") }
    private val TimetableOffsetFileV4: File by lazy { File(FilesFolder, "timetable4_offsets.bin") }
    private val TimetableOverridesFileV4: File by lazy { File(FilesFolder, "timetable4_overrides.csv") }
    //endregion


    companion object : KLogging() {
        private const val InnerSeparator: Char = '\u2004'

        private const val LatestVersion = 5
    }
}