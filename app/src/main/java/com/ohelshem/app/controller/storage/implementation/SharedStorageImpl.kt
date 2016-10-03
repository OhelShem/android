package com.ohelshem.app.controller.storage.implementation

import android.content.Context
import android.support.v7.app.AppCompatDelegate
import com.chibatching.kotpref.KotprefModel
import com.ohelshem.api.Role
import com.ohelshem.api.controller.implementation.ApiParserImpl
import com.ohelshem.api.model.Hour
import com.ohelshem.api.model.UserData
import com.ohelshem.app.controller.storage.IStorage.Companion.EmptyData
import com.ohelshem.app.controller.storage.SharedStorage
import com.ohelshem.app.controller.storage.SharedStorage.Theme
import com.ohelshem.app.controller.utils.OffsetDataController
import com.ohelshem.app.model.OverrideData
import mu.KLogging
import java.io.File
import java.util.*

class SharedStorageImpl(private val offsetDataController: OffsetDataController) : SharedStorage, KotprefModel() {
    override var version: Int by intPrefVar(EmptyData)

    override var id: String by stringPrefVar()
    override var password: String by stringPrefVar()

    override var notificationsForChanges: Boolean by booleanPrefVar()
    override var notificationsForHolidays: Boolean by booleanPrefVar()
    override var notificationsForTests: Boolean by booleanPrefVar()
    override var notificationsForTimetable: Boolean by booleanPrefVar()

    override var developerMode: Boolean by booleanPrefVar()

    override var changesDate: Long by longPrefVar(EmptyData.toLong())
    override var serverUpdateDate: Long by longPrefVar(EmptyData.toLong())
    override var updateDate: Long by longPrefVar(EmptyData.toLong())

    var _theme: Int by intPrefVar(Theme.Blue.ordinal)
    override var theme: Theme
        get() = Theme.values()[_theme]
        set(value) {
            _theme = value.ordinal
        }

    override var darkMode: Int by intPrefVar(AppCompatDelegate.MODE_NIGHT_YES)

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

    override var timetable: Array<Array<Hour>>?
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
                logger.error(e) { "Reading timetable failed" }
                return null
            }
        }
        set(value) {
            if (value == null || value.isEmpty()) {
                TimetableDataFile.delete()
                TimetableOffsetFile.delete()
                timetableListeners.values.forEach { it(emptyArray()) }
            } else {
                prepare()
                value.asSequence().flatMap { it.asSequence() }.map { it.name + InnerSeparator + it.teacher + InnerSeparator + it.color.toString() }.let {
                    offsetDataController.write(TimetableOffsetFile, TimetableDataFile, it)
                }
                timetableListeners.values.forEach { it(value) }
            }
        }
    //endregion

    //region Overrides
    private val overridesListeners: MutableMap<Int, (Array<OverrideData>) -> Unit> = HashMap(1)

    override fun attachOverridesListener(id: Int, listener: (Array<OverrideData>) -> Unit) {
        overridesListeners[id] = listener
    }

    override fun removeOverrideListener(id: Int) {
        overridesListeners.keys -= id
    }

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
            overridesListeners.values.forEach { it(value) }
        }


    private fun readOverrides(): Array<OverrideData> {
        if (!TimetableOverridesFile.exists()) return emptyArray()
        val data = TimetableOverridesFile.readLines()
        return Array(data.size) { i ->
            data[i].split('^').let { OverrideData(it[0].toInt(), it[1].toInt(), it[2], it[3]) }
        }
    }

    private fun setOverridesSilently(value: Array<OverrideData>) {
        prepare()
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
        prepare()
        if (TimetableOverridesFile.exists()) {
            TimetableOverridesFileBackup.createNewFile()
            TimetableOverridesFile.copyTo(TimetableOverridesFileBackup, overwrite = true)
        } else TimetableOverridesFile.createNewFile()
        file.copyTo(TimetableOverridesFile, overwrite = true)
        val data: Array<OverrideData> = try {
            readOverrides()
        } catch (e: Exception) {
            try {
                readOverridesOldWay().apply {
                    setOverridesSilently(this)
                }
            } catch (e: Exception) {
                if (TimetableOverridesFileBackup.exists())
                    TimetableOverridesFileBackup.copyTo(TimetableOverridesFile, overwrite = true)
                throw IllegalArgumentException()
            }
        } finally {
            TimetableOverridesFileBackup.delete()
        }
        overridesListeners.values.forEach { it(data) }
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
        Files.forEach { it.delete() }
    }

    override fun migration() {
        if (version == EmptyData) {
            val oldPrefsName = "DBControllerImpl.xml"
            val oldPrefsFile = File(SharedPreferencesFolder, oldPrefsName)
            if (oldPrefsFile.exists()) {
                val prefs = context.getSharedPreferences(oldPrefsName, Context.MODE_PRIVATE)
                val oldVersion = prefs.getInt("databaseVersion", EmptyData)
                if (oldVersion != 3) {
                    FilesFolder.deleteRecursively()
                    FilesFolder.mkdir()
                    oldPrefsFile.delete()
                } else {
                    val oldId = prefs.getString("_userIdentity", "")
                    if (oldId.isNotEmpty())
                        id = oldId

                    val oldPassword = prefs.getString("password", "")
                    if (oldPassword.isNotEmpty())
                        password = oldPassword

                    val oldDeveloperMode = prefs.getBoolean("developerModeEnabled", false)
                    developerMode = oldDeveloperMode

                    val oldChangesNotification = prefs.getBoolean("notificationsForChangesEnabled", false)
                    notificationsForChanges = oldChangesNotification

                    val oldHolidaysNotification = prefs.getBoolean("notificationsForHolidaysEnabled", false)
                    notificationsForHolidays = oldHolidaysNotification

                    val oldTestNotifications = prefs.getBoolean("notificationsForTestsEnabled", false)
                    notificationsForTests = oldTestNotifications

                    val oldTimetableNotifications = prefs.getBoolean("notificationsForTimetableEnabled", false)
                    notificationsForTimetable = oldTimetableNotifications
                }

                FilesFolder.deleteRecursively()
                FilesFolder.mkdir()
                oldPrefsFile.delete()
            }
        }
        version = 4
    }

    override fun prepare() {
        if (!FilesFolder.exists())
            FilesFolder.mkdir()
    }

    private val Files: Array<File> by lazy { arrayOf(TimetableDataFile, TimetableOffsetFile, TimetableOverridesFile, TimetableOverridesFileBackup) }

    private val FilesFolder: File by lazy { context.filesDir }

    private val TimetableDataFile: File by lazy { File(FilesFolder, "timetable3.bin") }
    private val TimetableOffsetFile: File by lazy { File(FilesFolder, "timetable3_offsets.bin") }

    private val TimetableOverridesFile: File by lazy { File(FilesFolder, "timetable_overrides.csv") }
    private val TimetableOverridesFileBackup: File by lazy { File(FilesFolder, "timetable_overrides_backup.csv") }

    private val SharedPreferencesFolder: File by lazy { File(context.filesDir.parent, "shared_prefs") }

    companion object : KLogging() {
        private const val InnerSeparator: Char = '\u2004'
    }
}