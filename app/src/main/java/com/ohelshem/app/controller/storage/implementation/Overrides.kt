package com.ohelshem.app.controller.storage.implementation

import com.ohelshem.app.controller.serialization.OverrideSerialization
import com.ohelshem.app.controller.serialization.ofList
import com.ohelshem.app.controller.serialization.simpleReader
import com.ohelshem.app.controller.serialization.simpleWriter
import com.ohelshem.app.model.OverrideData
import java.io.File

object Overrides {
    val Versions = intArrayOf(5, 4, 3)

    fun read(version: Int, file: File): List<OverrideData> {
        if (!file.exists()) return emptyList()
        return when (version) {
            1, 2, 3 -> readV3(file)
            4 -> readV4(file)
            else -> readV5(file)
        }
    }

    fun write(file: File, overrides: List<OverrideData>) {
        if (overrides.isEmpty()) file.delete()
        else
            file.simpleWriter().use { writer ->
                writer.writeInt(5)
                overridesSerialization.serialize(writer, overrides)
            }
    }

    private fun readV3(file: File): List<OverrideData> {
        val data = file.readLines()
        try {
            return (0 until data.size).map { i ->
                data[i].split(',').let { OverrideData(it[0].toInt(), it[1].toInt(), it[2], "") }
            }
        } catch(e: Exception) {
            file.delete()
            return emptyList()
        }
    }

    private fun readV4(file: File): List<OverrideData> {
        val data = file.readLines()
        return (0 until data.size).map { i ->
            data[i].split('^').let { OverrideData(it[0].toInt(), it[1].toInt(), it[2], it[3]) }
        }
    }

    private val overridesSerialization = OverrideSerialization.ofList()
    private fun readV5(file: File): List<OverrideData> = file.simpleReader().use { reader ->
        val version = reader.readInt()
        if (version != 5) throw IllegalArgumentException()
        overridesSerialization.deserialize(reader)
    }
}