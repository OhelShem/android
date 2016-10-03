package com.ohelshem.app.controller.storage

import android.graphics.Color
import com.ohelshem.api.model.Change
import com.ohelshem.app.clearTime
import com.ohelshem.app.getHour
import com.ohelshem.app.toCalendar
import java.util.*

class DeveloperOptions(private val storage: Storage) : Storage by storage {
    override var changesDate: Long
        get() {
            if (isFakingChanges)
                return if (getHour() < 21) Calendar.getInstance().clearTime().timeInMillis else Calendar.getInstance().clearTime().apply { add(Calendar.DAY_OF_YEAR, 1) }.timeInMillis
            else
                return storage.changesDate
        }
        set(value) {
            if (!isFakingChanges)
                storage.changesDate = value
        }

    override var serverUpdateDate: Long
        get() {
            if (isFakingChanges)
                return System.currentTimeMillis()
            else
                return storage.serverUpdateDate
        }
        set(value) {
            if (!isFakingChanges)
                storage.serverUpdateDate = value
        }

    override var changes: List<Change>?
        get() {
            if (isFakingChanges) {
                if (isFakingNoChanges) return emptyList()
                val clazz = userData.clazz
                val day = changesDate.toCalendar()[Calendar.DAY_OF_WEEK]
                val timetable = timetable ?: return emptyList()
                if (day > timetable.size || timetable[day - 1].size == 0) return emptyList()
                val size = timetable[day - 1].size
                var skip = false
                val changes = mutableListOf<Change>()
                repeat(size) {
                    if (!skip) {
                        changes += Change(clazz, it, "שינוי מזויף", Color.BLUE)
                    }
                    skip = !skip
                }
                return changes
            } else return storage.changes
        }
        set(value) {
            if (!isFakingChanges)
                storage.changes = changes
        }

    override fun hasChanges(clazz: Int): Boolean {
        return changes?.any { it.clazz == clazz } ?: false
    }

    companion object {
        var isFakingChanges = false
        var isFakingNoChanges = false

        fun fakeChanges() {
            isFakingChanges = true
            isFakingNoChanges = false
        }

        fun fakeNoChanges() {
            isFakingChanges = true
            isFakingNoChanges = true
        }

        fun stopFaking() {
            isFakingChanges = false
            isFakingNoChanges = false
        }
    }
}