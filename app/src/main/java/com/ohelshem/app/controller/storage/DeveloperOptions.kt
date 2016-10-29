package com.ohelshem.app.controller.storage

import com.ohelshem.api.model.Change
import com.ohelshem.app.android.App
import com.ohelshem.app.android.colorArrayRes
import com.ohelshem.app.clearTime
import com.ohelshem.app.getHour
import com.ohelshem.app.toCalendar
import com.yoavst.changesystemohelshem.R
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

    private val fakingChangesColors = App.instance.colorArrayRes(R.array.changesColors)
    override var changes: List<Change>?
        get() {
            if (isFakingChanges) {
                if (isFakingNoChanges) return emptyList()
                val clazz = userData.clazz
                val day = changesDate.toCalendar()[Calendar.DAY_OF_WEEK]
                val timetable = timetable ?: return emptyList()
                val size = if (day > timetable.size || timetable[day - 1].size == 0) 10 else timetable[day - 1].size

                var c = 0

                // change every 2nd hour for the user's class. Helps to test the class changes view.
                var skip = false
                val changes = mutableListOf<Change>()
                repeat(size) {
                    if (it + 1 <= 10) {
                        if (!skip) {
                            changes += Change(clazz, it + 1, "שינוי מזויף", fakingChangesColors[c])
                        }
                        skip = !skip
                    }
                }

                // Random changes for other classes
                if (clazz != 1) {
                    c++
                    val color = fakingChangesColors[c]
                    changes += Change(1, 5, "מקבץ בלי בכר", color)
                    changes += Change(1, 6, "מקבץ בלי בכר", color)
                }
                if (clazz != 3) {
                    c++
                    val color = fakingChangesColors[c]
                    changes += Change(3, 1, "מבוטל", color)
                    changes += Change(3, 5, "גולן בחדר 232", color)
                }
                if (clazz != 4) {
                    c++
                    val color = fakingChangesColors[c]
                    changes += Change(4, 1, "סיור כיתתי", color)
                    changes += Change(4, 2, "סיור כיתתי", color)
                    changes += Change(4, 3, "סיור כיתתי", color)
                    changes += Change(4, 4, "סיור כיתתי", color)
                    changes += Change(4, 5, "סיור כיתתי", color)
                    changes += Change(4, 6, "סיור כיתתי", color)
                    changes += Change(4, 7, "סיור כיתתי", color)
                    changes += Change(4, 8, "סיור כיתתי", color)
                    changes += Change(4, 9, "סיור כיתתי", color)
                    changes += Change(4, 10, "סיור כיתתי", color)
                }
                if (clazz != 6) {
                    c++
                    val color = fakingChangesColors[c]
                    changes += Change(6, 1, "חנג בלי גולן", color)
                    changes += Change(6, 2, "בספריה", color)
                }
                if (clazz != 8) {
                    c++
                    val color = fakingChangesColors[c]
                    changes += Change(8, 1, "מבוטל", color)
                    changes += Change(8, 2, "מבוטל", color)
                    changes += Change(8, 6, "בלי בן יעקב", color)
                    changes += Change(8, 7, "בלי בן יעקב", color)
                    changes += Change(8, 8, "בלי בן יעקב", color)
                }
                if (clazz != 9) {
                    c++
                    val color = fakingChangesColors[c]
                    changes += Change(9, 1, "שלח", color)
                    changes += Change(9, 2, "שלח", color)
                    changes += Change(9, 3, "שלח", color)
                    changes += Change(9, 4, "שלח", color)
                    changes += Change(9, 5, "שלח", color)
                    changes += Change(9, 6, "שלח", color)
                    changes += Change(9, 7, "שלח", color)
                    changes += Change(9, 8, "שלח", color)
                    changes += Change(9, 9, "שלח", color)
                    changes += Change(9, 10, "שלח", color)
                }
                if (clazz != 11) {
                    c++
                    val color = fakingChangesColors[c]
                    changes += Change(11, 1, "סדנא גוטמן בחדר 304", color)
                    changes += Change(11, 2, "סדנא מארק בחדר 304", color)
                    changes += Change(11, 3, "סדנא מארק בחדר 304", color)
                    changes += Change(11, 4, "סדנא שלטי בחדר 304", color)
                    changes += Change(11, 5, "סדנא שלטי בחדר 304", color)
                    changes += Change(11, 6, "סדנא גוטמן בחדר 304", color)
                }
                if (clazz != 12) {
                    c++
                    val color = fakingChangesColors[c]
                    changes += Change(12, 2, "הצגה באשכול", color)
                    changes += Change(12, 3, "הצגה באשכול", color)
                }
                return changes
            } else return storage.changes
        }
        set(value) {
            storage.changes = value
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