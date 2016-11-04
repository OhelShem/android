package com.ohelshem.app.controller.serialization

import com.ohelshem.api.model.Change
import com.ohelshem.api.model.Hour
import com.ohelshem.api.model.Test
import com.ohelshem.app.model.OverrideData

object HourSerialization : Serialization<Hour> {
    override fun serialize(writer: SimpleWriter, data: Hour) {
        writer.writeInt(data.color)
        writer.writeString(data.name)
        writer.writeString(data.teacher)
    }

    override fun deserialize(reader: SimpleReader): Hour {
        val color = reader.readInt()
        val name = reader.readString()
        val teacher = reader.readString()
        return Hour(name, teacher, color)
    }
}

object TestSerialization : Serialization<Test> {
    override fun serialize(writer: SimpleWriter, data: Test) {
        writer.writeLong(data.date)
        writer.writeString(data.content)
    }

    override fun deserialize(reader: SimpleReader): Test {
        val date = reader.readLong()
        val content = reader.readString()
        return Test(date, content)
    }
}

object ChangeSerialization : Serialization<Change> {
    override fun serialize(writer: SimpleWriter, data: Change) {
        writer.writeInt(data.clazz)
        writer.writeInt(data.hour)
        writer.writeInt(data.color)
        writer.writeString(data.content)
    }

    override fun deserialize(reader: SimpleReader): Change {
        val clazz = reader.readInt()
        val hour = reader.readInt()
        val color = reader.readInt()
        val content = reader.readString()
        return Change(clazz, hour, content, color)
    }
}

object OverrideSerialization : Serialization<OverrideData> {
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
        return OverrideData(day, hour, newName, newTeacher)
    }
}