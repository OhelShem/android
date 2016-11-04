package com.ohelshem.app.controller.serialization

import java.io.*
import java.util.*

interface Deserializer<out T : Any> {
    fun deserialize(reader: SimpleReader): T
}

interface Serializer<in T : Any> {
    fun serialize(writer: SimpleWriter, data: T)
}

interface Serialization<T: Any> : Serializer<T>, Deserializer<T>

interface SimpleWriter: Closeable {
    fun writeInt(value: Int)
    fun writeBool(value: Boolean)
    fun writeByte(value: Int)
    fun writeString(value: String)
    fun writeLong(value: Long)
}

interface SimpleReader: Closeable {
    fun readInt(): Int
    fun readBool(): Boolean
    fun readByte(): Byte
    fun readString(): String
    fun readLong(): Long
}

inline fun SimpleWriter.writeList(length: Int, writing: () -> Unit) {
    writeInt(length)
    repeat(length) {
        writing()
    }
}

inline fun <K> SimpleWriter.writeList(list: List<K>, writing: (K) -> Unit) {
    writeInt(list.size)
    list.forEach {
        writing(it)
    }
}

inline fun <K> SimpleReader.readList(reading: () -> K): List<K> {
    val length = readInt()
    val list = ArrayList<K>(length)
    repeat(length) {
        list += reading()
    }
    return list
}

class ListSerialization<K : Any>(val serializer: Serializer<K>, val deserializer: Deserializer<K>) : Serialization<List<K>> {
    override fun serialize(writer: SimpleWriter, data: List<K>) = writer.writeList(data) { serializer.serialize(writer, it) }

    override fun deserialize(reader: SimpleReader): List<K> = reader.readList { deserializer.deserialize(reader) }
}

fun <K : Any> Serialization<K>.ofList(): Serialization<List<K>> {
    @Suppress("UNCHECKED_CAST")
    return ListSerialization(this, this)
}

class DataOutputStreamWriter(private val stream: DataOutputStream) : SimpleWriter {
    override fun writeInt(value: Int) = stream.writeInt(value)

    override fun writeByte(value: Int) = stream.writeByte(value)

    override fun writeBool(value: Boolean) = stream.writeBoolean(value)

    override fun writeString(value: String) = stream.writeUTF(value)

    override fun writeLong(value: Long) = stream.writeLong(value)

    override fun close() = stream.close()
}

class DataOutputStreamReader(private val stream: DataInputStream) : SimpleReader {
    override fun readInt(): Int = stream.readInt()

    override fun readByte(): Byte = stream.readByte()

    override fun readBool(): Boolean = stream.readBoolean()

    override fun readString(): String = stream.readUTF()

    override fun readLong(): Long = stream.readLong()

    override fun close() = stream.close()
}

fun File.simpleWriter() = DataOutputStreamWriter(DataOutputStream(outputStream()))
fun File.simpleReader() = DataOutputStreamReader(DataInputStream(inputStream()))
fun InputStream.simpleReader() = DataOutputStreamReader(DataInputStream(this))
