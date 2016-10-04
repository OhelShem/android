package com.ohelshem.app.controller.storage.implementation

import com.chibatching.kotpref.KotprefModel
import com.ohelshem.api.model.Change
import com.ohelshem.api.model.Test
import com.ohelshem.app.controller.storage.IStorage.Companion.EmptyData
import com.ohelshem.app.controller.storage.StudentStorage
import com.ohelshem.app.controller.utils.OffsetDataController
import java.io.File
import java.util.*

class StudentStorageImpl(private val offsetDataController: OffsetDataController) : StudentStorage, KotprefModel() {
    override var version: Int by intPrefVar(EmptyData)

    override var changes: List<Change>?
        get() {
            if (!ChangesDataFile.exists() || !ChangesOffsetFile.exists()) return null
            val data = offsetDataController.read(ChangesOffsetFile, ChangesDataFile, OffsetDataController.AllFile)
            val list = ArrayList<Change>(data.size)
            for (i in 0 until data.size) {
                list += data[i].split(InnerSeparator, limit = 4).let { Change(it[0].toInt(), it[1].toInt(), it[2], it[3].toInt()) }
            }
            return list
        }
        set(value) {
            if (value == null || value.isEmpty()) {
                ChangesDataFile.delete()
                ChangesOffsetFile.delete()
            } else {
                prepare()
                value.asSequence().map { it.clazz.toString() + InnerSeparator + it.hour.toString() + InnerSeparator + it.content + InnerSeparator + it.color.toString() }.let {
                    offsetDataController.write(ChangesOffsetFile, ChangesDataFile, it)
                }
            }
        }

    override var tests: List<Test>?
        get() {
            if (!TestsDataFile.exists() || !TestsOffsetFile.exists()) return null
            val data = offsetDataController.read(TestsOffsetFile, TestsDataFile, OffsetDataController.AllFile)
            val list = ArrayList<Test>(data.size)
            for (i in 0 until data.size) {
                list += data[i].split(InnerSeparator, limit = 3).let { Test(it[0].toLong(), it[1]) }
            }
            return list
        }
        set(value) {
            if (value == null) {
                TestsDataFile.delete()
                TestsOffsetFile.delete()
            } else {
                prepare()
                value.asSequence().map { it.date.toString() + InnerSeparator + it.content.toString() }.let {
                    offsetDataController.write(TestsOffsetFile, TestsDataFile, it)
                }
            }
        }



    override fun migration() {
        if (version == EmptyData) {
            tests = emptyList()
            version = 4
        }
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

    private val Files: Array<File> by lazy { arrayOf(ChangesDataFile, ChangesOffsetFile, TestsDataFile, TestsOffsetFile) }

    private val FilesFolder: File by lazy { context.filesDir }

    private val ChangesDataFile: File by lazy { File(FilesFolder, "changes4.bin") }
    private val ChangesOffsetFile: File by lazy { File(FilesFolder, "changes4_offsets.bin") }

    private val TestsDataFile: File by lazy { File(FilesFolder, "tests4.bin") }
    private val TestsOffsetFile: File by lazy { File(FilesFolder, "tests4_offsets.bin") }

    companion object {
        private const val InnerSeparator: Char = '\u2004'
    }

}