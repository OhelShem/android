package com.ohelshem.app.controller.serialization

import com.ohelshem.api.model.Change
import com.ohelshem.api.model.ClassInfo
import com.ohelshem.api.model.Hour
import com.ohelshem.api.model.Test
import com.ohelshem.app.model.Contact
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

object ContactSerialization : Serialization<Contact> {
    override fun serialize(writer: SimpleWriter, data: Contact) {
        writer.writeBool(data.gender)
        writer.writeByte(data.layer)
        writer.writeByte(data.clazz)
        writer.writeLong(data.birthday)
        writer.writeInt(data.phone.replace("-", "").let { if (it.isEmpty()) "0" else it }.toInt())
        data.name.mapToDeltasForContacts().forEach { char ->
            writer.writeByte(char)
        }
    }

    override fun deserialize(reader: SimpleReader): Contact {
        val gender = reader.readBool()
        val layer = reader.readByte().toInt()
        val clazz = reader.readByte().toInt()
        val birthday = reader.readLong()
        val phoneNumber = reader.readInt().toPhoneNumber()
        val name = reader.readList { reader.readByte().fromDelta() }.joinToString("")
        return Contact(name, layer, clazz, gender, birthday, phoneNumber)
    }
}

private const val firstLetter = 'א'
private const val firstLetterValue = firstLetter.toInt()
private fun String.mapToDeltasForContacts() = IntArray(length) {
    val char = get(it)
    when (char) {
        in 'א'..'ת' -> 10 + (char - firstLetter)
        ' ' -> 0
        '(' -> 1
        ')' -> 2
        '-' -> 3
        '\'' -> 4
        else -> 5
    }
}

private fun Byte.fromDelta(): Char {
    val value = this.toInt()
    return when (value) {
        0, 5 -> ' '
        1 -> '('
        2 -> ')'
        3 -> '-'
        4 -> '\''
        else -> (value - 10 + firstLetterValue).toChar()
    }

}

private fun Int.toPhoneNumber(): String = if (this == 0) "" else "0$this"

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

object ClassInfoSerialization : Serialization<ClassInfo> {
    override fun serialize(writer: SimpleWriter, data: ClassInfo) {
        writer.writeInt(data.layer)
        writer.writeInt(data.clazz)
    }

    override fun deserialize(reader: SimpleReader): ClassInfo {
        val layer = reader.readInt()
        val clazz = reader.readInt()
        return ClassInfo(layer, clazz)
    }
}
