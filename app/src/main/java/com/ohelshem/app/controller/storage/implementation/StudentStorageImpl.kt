package com.ohelshem.app.controller.storage.implementation

import com.chibatching.kotpref.KotprefModel
import com.ohelshem.api.model.Change
import com.ohelshem.api.model.Test
import com.ohelshem.app.controller.serialization.*
import com.ohelshem.app.controller.storage.IStorage.Companion.EmptyData
import com.ohelshem.app.controller.storage.StudentStorage
import com.ohelshem.app.controller.utils.OffsetDataController
import java.io.File
import java.util.*

class StudentStorageImpl(private val offsetDataController: OffsetDataController) : StudentStorage, KotprefModel() {
    override var version: Int by intPref(EmptyData)

    private val changesSerialization = ChangeSerialization.ofList()
    override var changes: List<Change>?
        get() {
            if (!ChangesDataFile.exists()) return null
            return ChangesDataFile.simpleReader().use { reader -> changesSerialization.deserialize(reader) }
        }
        set(value) {
            if (value == null || value.isEmpty()) {
                ChangesDataFile.delete()
            } else {
                prepare()
                ChangesDataFile.simpleWriter().use { writer -> changesSerialization.serialize(writer, value) }
            }
        }
    private val testsDeserialization = TestSerialization.ofList()
    override var tests: List<Test>?
        get() {
            if (!TestsDataFile.exists()) return null
            return TestsDataFile.simpleReader().use { reader -> testsDeserialization.deserialize(reader) }
        }
        set(value) {
            if (value == null || value.isEmpty()) {
                TestsDataFile.delete()
            } else {
                prepare()
                TestsDataFile.simpleWriter().use { writer -> testsDeserialization.serialize(writer, value) }
            }
        }

    override fun migration() {
        if (version == EmptyData) {
            tests = emptyList()
            version = LatestVersion
        } else if (version == 4) {
            // Migrate tests
            if (TestsDataFileV4.exists() && TestsOffsetFileV4.exists()) {
                val data = offsetDataController.read(TestsOffsetFileV4, TestsDataFileV4, OffsetDataController.AllFile)
                val list = ArrayList<Test>(data.size)
                repeat(data.size) { i ->
                    list += data[i].split(InnerSeparator, limit = 3).let { Test(it[0].toLong(), it[1]) }
                }
                TestsDataFileV4.delete()
                TestsOffsetFileV4.delete()
                this.tests = list
            }
            // Migrate changes
            if (ChangesDataFileV4.exists() && ChangesOffsetFileV4.exists()) {
                val data = offsetDataController.read(ChangesOffsetFileV4, ChangesDataFileV4, OffsetDataController.AllFile)
                val list = ArrayList<Change>(data.size)
                repeat(data.size) { i ->
                    list += data[i].split(InnerSeparator, limit = 4).let { Change(it[0].toInt(), it[1].toInt(), it[2], it[3].toInt()) }
                }
                ChangesDataFileV4.delete()
                ChangesOffsetFileV4.delete()
                this.changes = list
            }
        }
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

    private val Files: Array<File> by lazy { arrayOf(TestsDataFile, ChangesDataFile) }

    private val FilesFolder: File by lazy { context.filesDir }

    private val ChangesDataFile: File by lazy { File(FilesFolder, "changes5.bin") }
    private val TestsDataFile: File by lazy { File(FilesFolder, "tests5.bin") }


    //region compatibility
    private val ChangesDataFileV4: File by lazy { File(FilesFolder, "changes4.bin") }
    private val ChangesOffsetFileV4: File by lazy { File(FilesFolder, "changes4_offsets.bin") }
    private val TestsDataFileV4: File by lazy { File(FilesFolder, "tests4.bin") }
    private val TestsOffsetFileV4: File by lazy { File(FilesFolder, "tests4_offsets.bin") }
    //endregion

    companion object {
        private const val InnerSeparator: Char = '\u2004'

        private const val LatestVersion = 5
    }

}