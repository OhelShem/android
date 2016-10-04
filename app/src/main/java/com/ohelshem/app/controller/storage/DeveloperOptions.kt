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
                storage.changesDate = value
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
                changes += Change(9, 0, "שלח", Color.BLUE)
                changes += Change(9, 1, "שלח", Color.BLUE)
                changes += Change(9, 2, "שלח", Color.BLUE)
                changes += Change(9, 3, "שלח", Color.BLUE)
                changes += Change(9, 4, "שלח", Color.BLUE)
                changes += Change(9, 5, "שלח", Color.BLUE)
                changes += Change(9, 6, "שלח", Color.BLUE)
                changes += Change(9, 7, "שלח", Color.BLUE)
                changes += Change(9, 8, "שלח", Color.BLUE)
                changes += Change(9, 9, "שלח", Color.BLUE)
                changes += Change(12, 2, "הצגה באשכול", Color.YELLOW)
                changes += Change(12, 3, "הצגה באשכול", Color.YELLOW)
                changes += Change(1, 4, "מקבץ בלי בכר", Color.GREEN)
                changes += Change(1, 5, "מקבץ בלי בכר", Color.GREEN)
                changes += Change(8, 0, "מבוטל", Color.RED)
                changes += Change(8, 1, "מבוטל", Color.RED)
                changes += Change(8, 6, "בלי בן יעקב", Color.GREEN)
                changes += Change(8, 7, "בלי בן יעקב", Color.GREEN)
                changes += Change(8, 8, "בלי בן יעקב", Color.GREEN)
                changes += Change(6, 0, "חנג בלי גולן", Color.GREEN)
                changes += Change(6, 1, "בספריה", Color.GREEN)
                changes += Change(4, 0, "סיור כיתתי", Color.BLUE)
                changes += Change(4, 1, "סיור כיתתי", Color.BLUE)
                changes += Change(4, 2, "סיור כיתתי", Color.BLUE)
                changes += Change(4, 3, "סיור כיתתי", Color.BLUE)
                changes += Change(4, 4, "סיור כיתתי", Color.BLUE)
                changes += Change(4, 5, "סיור כיתתי", Color.BLUE)
                changes += Change(4, 6, "סיור כיתתי", Color.BLUE)
                changes += Change(4, 7, "סיור כיתתי", Color.BLUE)
                changes += Change(4, 8, "סיור כיתתי", Color.BLUE)
                changes += Change(4, 9, "סיור כיתתי", Color.BLUE)
                changes += Change(3, 0, "מבוטל", Color.RED)
                changes += Change(3, 4, "גולן בחדר 232", Color.GREEN)
                changes += Change(11, 0, "סדנא גוטמן בחדר 304", Color.YELLOW)
                changes += Change(11, 1, "סדנא גוטמן בחדר 304", Color.YELLOW)
                changes += Change(11, 2, "סדנא מארק בחדר 304", Color.YELLOW)
                changes += Change(11, 3, "סדנא מארק בחדר 304", Color.YELLOW)
                changes += Change(11, 4, "סדנא שלטי בחדר 304", Color.YELLOW)
                changes += Change(11, 5, "סדנא שלטי בחדר 304", Color.YELLOW)
                /*repeat(size) {
                    if (!skip) {

                    }
                    skip = !skip
                } */
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