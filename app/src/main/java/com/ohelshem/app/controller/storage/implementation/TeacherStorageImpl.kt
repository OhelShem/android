package com.ohelshem.app.controller.storage.implementation

import com.chibatching.kotpref.KotprefModel
import com.ohelshem.api.model.ClassInfo
import com.ohelshem.api.model.SchoolChange
import com.ohelshem.api.model.SchoolHour
import com.ohelshem.api.model.SchoolTest
import com.ohelshem.app.controller.info.SchoolInfo
import com.ohelshem.app.controller.storage.IStorage
import com.ohelshem.app.controller.storage.TeacherStorage
import com.ohelshem.app.controller.utils.OffsetDataController
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.io.File

class TeacherStorageImpl(private val offsetDataController: OffsetDataController, private val schoolInfo: SchoolInfo) : TeacherStorage, KotprefModel() {
    override var version: Int by intPrefVar(IStorage.EmptyData)
    override var classes: List<ClassInfo>
        get() {
            if (!ClassesFile.exists()) return emptyList()
            else {
                return DataInputStream(ClassesFile.inputStream()).use {
                    val list = mutableListOf<ClassInfo>()
                    while (true) {
                        try {
                            val layer = it.readInt()
                            val clazz = it.readInt()
                            list += ClassInfo(layer, clazz)
                        } catch(ignored: EOFException) {
                            break
                        }
                    }
                    list
                }
            }
        }
        set(value) {
            if (classes.isEmpty()) ClassesFile.delete()
            else {
                DataOutputStream(ClassesFile.outputStream()).use {
                    value.forEach { info ->
                        it.writeInt(info.layer)
                        it.writeInt(info.clazz)
                    }
                }
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

    override fun setSchoolChanges(changes: List<SchoolChange>?) {
        if (changes == null || changes.isEmpty()) {
            SchoolChangesDataFile.delete()
            SchoolChangesOffsetFile.delete()
        } else {
            prepare()
            changes.asSequence().map { it.layer.toString() + InnerSeparator + it.clazz.toString() + InnerSeparator + it.hour.toString() + InnerSeparator + it.content + InnerSeparator + it.color.toString() }.let {
                offsetDataController.write(SchoolChangesOffsetFile, SchoolChangesDataFile, it)
            }
        }
    }

    override fun getChangesForClass(layer: Int, clazz: Int): List<SchoolChange> {
        if (!schoolInfo.validate(layer, clazz)) return emptyList()

        if (!SchoolChangesDataFile.exists() || !SchoolChangesOffsetFile.exists()) return emptyList()
        return offsetDataController.read(SchoolChangesOffsetFile, SchoolChangesDataFile, schoolInfo.getAbsoluteClass(layer, clazz) - 1).map { item ->
            item.split(InnerSeparator, limit = 5).let { SchoolChange(it[0].toInt(), it[1].toInt(), it[2].toInt(), it[3], it[4].toInt()) }
        }
    }

    override val hasSchoolChanges: Boolean
        get() = SchoolChangesOffsetFile.exists() && SchoolChangesDataFile.exists()

    override fun setSchoolTests(tests: List<SchoolTest>?) {
        if (tests == null || tests.isEmpty()) {
            SchoolTestsDataFile.delete()
            SchoolTestOffsetFile.delete()
        } else {
            prepare()
            tests.asSequence().map { it.layer.toString() + InnerSeparator + it.clazz.toString() + InnerSeparator + it.date.toString() + InnerSeparator + it.content }.let {
                offsetDataController.write(SchoolChangesOffsetFile, SchoolChangesDataFile, it)
            }
        }
    }

    override fun getTestsForClass(layer: Int, clazz: Int): List<SchoolTest> {
        if (!schoolInfo.validate(layer, clazz)) return emptyList()

        if (!SchoolTestsDataFile.exists() || !SchoolTestOffsetFile.exists()) return emptyList()
        return offsetDataController.read(SchoolTestOffsetFile, SchoolTestsDataFile, schoolInfo.getAbsoluteClass(layer, clazz) - 1).map { item ->
            item.split(InnerSeparator, limit = 4).let { SchoolTest(it[0].toInt(), it[1].toInt(), it[2].toLong(), it[3]) }
        }
    }

    override val hasSchoolTests: Boolean
        get() = SchoolTestOffsetFile.exists() && SchoolTestsDataFile.exists()

    override fun setSchoolTimetable(timetable: List<SchoolHour>?) {
        if (timetable == null || timetable.isEmpty()) {
            SchoolTestsDataFile.delete()
            SchoolTestOffsetFile.delete()
        } else {
            prepare()
            timetable.asSequence().map {
                it.layer.toString() + InnerSeparator + it.clazz.toString() + InnerSeparator + it.day.toString() + InnerSeparator + it.hour + InnerSeparator +
                        it.name + InnerSeparator + it.teacher + InnerSeparator + it.color.toString()
            }.let {
                offsetDataController.write(SchoolChangesOffsetFile, SchoolChangesDataFile, it)
            }
        }
    }

    override fun getTimetableForClass(layer: Int, clazz: Int): List<SchoolHour> {
        if (!schoolInfo.validate(layer, clazz)) return emptyList()

        if (!SchoolTimetableDataFile.exists() || !SchoolTimetableOffsetFile.exists()) return emptyList()
        return offsetDataController.read(SchoolTimetableOffsetFile, SchoolTimetableDataFile, schoolInfo.getAbsoluteClass(layer, clazz) - 1).map { item ->
            item.split(InnerSeparator, limit = 7).let { SchoolHour(it[0].toInt(), it[1].toInt(), it[2].toInt(), it[3].toInt(), it[4], it[5], it[6].toInt()) }
        }
    }

    override val hasSchoolTimetable: Boolean
        get() = SchoolTimetableOffsetFile.exists() && SchoolTimetableDataFile.exists()


    override fun migration() {
        version = 4
    }

    override fun clean() {
        clear()
        version = 4
        Files.forEach { it.delete() }
    }

    override fun prepare() {
        if (!FilesFolder.exists())
            FilesFolder.mkdir()
    }

    private val Files: Array<File> by lazy {
        arrayOf(SchoolChangesDataFile, SchoolChangesOffsetFile,
                SchoolTestsDataFile, SchoolTestOffsetFile,
                SchoolTimetableDataFile, SchoolTimetableOffsetFile,
                ClassesFile)
    }

    private val FilesFolder: File by lazy { context.filesDir }

    private val SchoolChangesDataFile: File by lazy { File(FilesFolder, "school_changes.bin") }
    private val SchoolChangesOffsetFile: File by lazy { File(FilesFolder, "school_changes_offsets.bin") }

    private val SchoolTestsDataFile: File by lazy { File(FilesFolder, "school_tests.bin") }
    private val SchoolTestOffsetFile: File by lazy { File(FilesFolder, "school_tests_offsets.bin") }

    private val SchoolTimetableDataFile: File by lazy { File(FilesFolder, "school_timetable.bin") }
    private val SchoolTimetableOffsetFile: File by lazy { File(FilesFolder, "school_timetable_offsets.bin") }

    private val ClassesFile: File by lazy { File(FilesFolder, "teacher_classes.bin") }


    companion object {
        private const val InnerSeparator: Char = '\u2004'
    }
}