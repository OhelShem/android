package com.ohelshem.app.controller.storage

import com.ohelshem.api.model.ClassInfo
import com.ohelshem.api.model.SchoolChange
import com.ohelshem.api.model.SchoolHour
import com.ohelshem.api.model.SchoolTest

interface TeacherStorage : IStorage {
    var classes: List<ClassInfo>
    var primaryClass: ClassInfo?


    fun setSchoolTimetable(timetable: List<SchoolHour>?)
    fun getTimetableForClass(layer: Int, clazz: Int): List<SchoolHour>
    val hasSchoolTimetable: Boolean

    fun setSchoolTests(tests: List<SchoolTest>?)
    fun getTestsForClass(layer: Int, clazz: Int): List<SchoolTest>
    val hasSchoolTests: Boolean

    fun setSchoolChanges(changes: List<SchoolChange>?)
    fun getChangesForClass(layer: Int, clazz: Int): List<SchoolChange>
    val hasSchoolChanges: Boolean

    fun hasChanges(layer: Int, clazz: Int) = getChangesForClass(layer, clazz).isNotEmpty()
}
