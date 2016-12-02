package com.ohelshem.app.controller.storage.implementation

import com.chibatching.kotpref.KotprefModel
import com.ohelshem.api.model.*
import com.ohelshem.app.controller.info.SchoolInfo
import com.ohelshem.app.controller.serialization.*
import com.ohelshem.app.controller.storage.IStorage
import com.ohelshem.app.controller.storage.TeacherStorage
import java.io.File

class TeacherStorageImpl(private val schoolInfo: SchoolInfo) : TeacherStorage, KotprefModel() {
    override var version: Int by intPrefVar(IStorage.EmptyData)

    private val classInfoSerialization = ClassInfoSerialization.ofList()
    override var classes: List<ClassInfo>
        get() {
            if (!ClassesFile.exists()) return emptyList()
            else return ClassesFile.simpleReader().use { reader -> classInfoSerialization.deserialize(reader) }
        }
        set(value) {
            if (value.isEmpty()) ClassesFile.delete()
            else {
                prepare()
                ClassesFile.simpleWriter().use { writer -> classInfoSerialization.serialize(writer, value) }
            }
        }

    //region Primary class
    override var primaryClass: ClassInfo?
        get() {
            if (_primaryClassClass == -1 || _primaryClassLayer == -1) return null
            else return ClassInfo(_primaryClassLayer, _primaryClassClass)
        }
        set(value) {
            if (value == null) {
                _primaryClassClass = -1
                _primaryClassLayer = -1
            } else {
                _primaryClassLayer = value.layer
                _primaryClassClass = value.clazz
            }
        }

    private var _primaryClassLayer: Int by intPrefVar(-1)
    private var _primaryClassClass: Int by intPrefVar(-1)
    //endregion

    //region Changes
    private val SchoolChangesSerialization = SchoolChangeSerialization.ofList().toIndexed()

    override fun setSchoolChanges(changes: List<SchoolChange>?) {
        if (changes == null || changes.isEmpty()) {
            SchoolChangesFile.delete()
        } else {
            prepare()
            SchoolChangesFile.simpleWriter().use { writer -> SchoolChangesSerialization.serialize(writer, changes.layerFlatMap()) }
        }
    }

    override fun getChangesForClass(layer: Int, clazz: Int): List<SchoolChange> {
        if (!schoolInfo.validate(layer, clazz)) return emptyList()

        if (!SchoolChangesFile.exists()) return emptyList()
        return SchoolChangesFile.simpleReader().use { reader -> SchoolChangesSerialization.deserialize(schoolInfo.getAbsoluteClass(layer, clazz), reader) }
    }

    override val hasSchoolChanges: Boolean
        get() = SchoolChangesFile.exists()
    //endregion

    //region Tests
    private val SchoolTestsSerialization = SchoolTestSerialization.ofList().toIndexed()

    override fun setSchoolTests(tests: List<SchoolTest>?) {
        if (tests == null || tests.isEmpty()) {
            SchoolTestsFile.delete()
        } else {
            prepare()
            SchoolTestsFile.simpleWriter().use { writer -> SchoolTestsSerialization.serialize(writer, tests.layerFlatMap()) }
        }
    }

    override fun getTestsForClass(layer: Int, clazz: Int): List<SchoolTest> {
        if (!schoolInfo.validate(layer, clazz)) return emptyList()

        if (!SchoolTestsFile.exists()) return emptyList()
        return SchoolTestsFile.simpleReader().use { reader -> SchoolTestsSerialization.deserialize(schoolInfo.getAbsoluteClass(layer, clazz), reader) }
    }

    override val hasSchoolTests: Boolean
        get() = SchoolTestsFile.exists()
    //endregion

    //region Timetable
    private val SchoolTimetableSerialization = SchoolHourSerialization.ofList().toIndexed()

    override fun setSchoolTimetable(timetable: List<SchoolHour>?) {
        if (timetable == null || timetable.isEmpty()) {
            SchoolTimetableFile.delete()
        } else {
            prepare()
            SchoolTimetableFile.simpleWriter().use { writer -> SchoolTimetableSerialization.serialize(writer, timetable.layerFlatMap()) }
        }
    }

    override fun getTimetableForClass(layer: Int, clazz: Int): List<SchoolHour> {
        if (!schoolInfo.validate(layer, clazz)) return emptyList()

        if (!SchoolTimetableFile.exists()) return emptyList()
        return SchoolTimetableFile.simpleReader().use { reader -> SchoolTimetableSerialization.deserialize(schoolInfo.getAbsoluteClass(layer, clazz), reader) }
    }

    override val hasSchoolTimetable: Boolean
        get() = SchoolTimetableFile.exists()
    //endregion


    override fun migration() {
        version = LatestVersion
    }

    override fun clean() {
        clear()
        version = LatestVersion
        Files.forEach { it.delete() }
    }

    override fun prepare() {
        if (!FilesFolder.exists())
            FilesFolder.mkdir()
    }

    private val Files: Array<File> by lazy {
        arrayOf(SchoolChangesFile,
                SchoolTestsFile,
                SchoolTimetableFile,
                ClassesFile)
    }

    private val FilesFolder: File by lazy { context.filesDir }

    private val SchoolChangesFile: File by lazy { File(FilesFolder, "school_changes_v5.bin") }

    private val SchoolTestsFile: File by lazy { File(FilesFolder, "school_tests_v5.bin") }

    private val SchoolTimetableFile: File by lazy { File(FilesFolder, "school_timetable_v5.bin") }

    private val ClassesFile: File by lazy { File(FilesFolder, "teacher_classes.bin") }

    private fun <K : SchoolModel> List<K>.layerFlatMap(): List<List<K>> {
        val layers = Array(4) { i -> Array(schoolInfo[i + 9]) { mutableListOf<K>() }.toList() }.toList()
        forEach {
            layers[it.layer - 9][it.clazz - 1] += it
        }
        return layers.flatten()
    }

    companion object {
        private const val LatestVersion = 5
    }
}