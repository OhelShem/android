package com.ohelshem.app.controller.storage

import android.support.v7.app.AppCompatDelegate.NightMode
import com.ohelshem.api.model.Hour
import com.ohelshem.api.model.UserData
import com.ohelshem.app.model.OverrideData
import java.io.File

interface SharedStorage : IStorage {
    var id: String
    var password: String

    var serverUpdateDate: Long
    var changesDate: Long
    var updateDate: Long

    var userData: UserData
    var timetable: Array<Array<Hour>>?

    var notificationsForChanges: Boolean
    var notificationsForTests: Boolean
    var notificationsForHolidays: Boolean
    var notificationsForBirthdays: Boolean
    var notificationsForTimetable: Boolean

    var developerMode: Boolean
    var appVersion: Int
    var debugFlag: Boolean

    var theme: Theme

    @NightMode
    var darkMode: Int

    fun attachTimetableListener(id: Int, listener: (Array<Array<Hour>>) -> Unit)
    fun removeTimetableListener(id: Int)

    var overrides: List<OverrideData>

    fun attachOverridesListener(id: Int, listener: (List<OverrideData>) -> Unit)
    fun removeOverrideListener(id: Int)

    fun importOverrideFile(file: File)
    fun exportOverrideFile(file: File): Boolean

    fun isSetup(): Boolean = id.isNotEmpty()

    fun isStudent(): Boolean = !userData.isTeacher()

    enum class Theme {
        Red, Green, Blue
    }
}