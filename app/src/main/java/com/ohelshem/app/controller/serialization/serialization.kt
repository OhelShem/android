package com.ohelshem.app.controller.serialization

import java.io.Closeable
import java.util.*

interface Deserializer<out T : Any> {
    fun deserialize(reader: SimpleReader): T
}

interface Serializer<in T : Any> {
    fun serialize(writer: SimpleWriter, data: T)
}

interface Serialization<T : Any> : Serializer<T>, Deserializer<T>

interface SimpleWriter : Closeable {
    val size: Int

    fun writeInt(value: Int)
    fun writeBool(value: Boolean)
    fun writeByte(value: Int)
    fun writeByteArray(value: ByteArray)
    fun writeString(value: String)
    fun writeLong(value: Long)
}

interface SimpleReader : Closeable {
    fun readInt(): Int
    fun readBool(): Boolean
    fun readByte(): Byte
    fun readString(): String
    fun readLong(): Long

    fun skip(bytes: Int): Int
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

inline fun <K> SimpleReader.readListFiltered(reading: () -> K, filter: (K) -> Boolean): List<K> {
    val length = readInt()
    val list = ArrayList<K>(length / 10)
    repeat(length) {
        val value = reading()
        if (filter(value))
            list += value
    }
    return list
}

open class ListSerialization<K : Any>(val serializer: Serializer<K>, val deserializer: Deserializer<K>) : Serialization<List<K>> {
    override fun serialize(writer: SimpleWriter, data: List<K>) = writer.writeList(data) { serializer.serialize(writer, it) }

    override fun deserialize(reader: SimpleReader): List<K> = reader.readList { deserializer.deserialize(reader) }
}

class FilterListSerialization<K : Any>(val serialization: ListSerialization<K>, val filter: (K) -> Boolean) : Serialization<List<K>> by serialization {
    override fun deserialize(reader: SimpleReader): List<K> {
        return reader.readListFiltered({ serialization.deserializer.deserialize(reader) }, filter)
    }
}

fun <K : Any> Serialization<K>.ofList(): ListSerialization<K> = ListSerialization(this, this)

fun <K : Any> ListSerialization<K>.filter(filter: (K) -> Boolean) = FilterListSerialization(this, filter)