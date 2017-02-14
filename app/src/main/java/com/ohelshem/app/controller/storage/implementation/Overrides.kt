package com.ohelshem.app.controller.storage.implementation

import com.ohelshem.app.controller.serialization.*
import com.ohelshem.app.model.OverrideData
import java.io.File

object Overrides {
    val Versions = intArrayOf(6, 5, 4, 3)

    fun read(version: Int, file: File): List<OverrideData> {
        if (!file.exists()) return emptyList()
        return when (version) {
            1, 2, 3 -> readV3(file)
            4 -> readV4(file)
            5 -> readV5(file)
            else -> readV6(file)
        }
    }

    fun write(file: File, overrides: List<OverrideData>) {
        if (overrides.isEmpty()) file.delete()
        else
            file.simpleWriter().use { writer ->
                writer.writeInt(6)
                overridesSerialization.serialize(writer, overrides)
            }
    }

    private fun readV3(file: File): List<OverrideData> {
        val data = file.readLines()
        try {
            return List(data.size) { i ->
                data[i].split(',').let { OverrideData(it[0].toInt(), it[1].toInt(), it[2], "", 0) }
            }
        } catch(e: Exception) {
            file.delete()
            return emptyList()
        }
    }

    private fun readV4(file: File): List<OverrideData> {
        val data = file.readLines()
        return List(data.size) { i ->
            data[i].split('^').let { OverrideData(it[0].toInt(), it[1].toInt(), it[2], it[3], 0) }
        }
    }

    private val V5overridesSerialization = object : Serialization<OverrideData> {
        override fun serialize(writer: SimpleWriter, data: OverrideData) {
            writer.writeInt(data.day)
            writer.writeInt(data.hour)
            writer.writeString(data.newName)
            writer.writeString(data.newTeacher)
        }

        override fun deserialize(reader: SimpleReader): OverrideData {
            val day = reader.readInt()
            val hour = reader.readInt()
            val newName = reader.readString()
            val newTeacher = reader.readString()
            return OverrideData(day, hour, newName, newTeacher, 0)
        }
    }.ofList()

    private fun readV5(file: File): List<OverrideData> = file.simpleReader().use { reader ->
        val version = reader.readInt()
        if (version != 5) throw IllegalArgumentException()
        V5overridesSerialization.deserialize(reader)
    }

    private val overridesSerialization = OverrideSerialization.ofList()
    private fun readV6(file: File): List<OverrideData> = file.simpleReader().use { reader ->
        val version = reader.readInt()
        if (version != 6) throw IllegalArgumentException()
        overridesSerialization.deserialize(reader)
    }
}