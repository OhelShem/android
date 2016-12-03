package com.ohelshem.app.controller.serialization

import java.io.ByteArrayOutputStream

interface IndexedDeserializer<out T : Any> : Deserializer<List<T>> {
    fun deserialize(index: Int, reader: SimpleReader): T
}

interface IndexedSerializer<in T : Any> : Serializer<List<T>>

interface IndexedDeserialization<T : Any> : Serialization<List<T>>, IndexedDeserializer<T>, IndexedSerializer<T>

/**
 * Deserialize data using indexes.
 *
 * Data structure:
 * [4] Length of header
 * [4] Number of items in header
 * [4] <-> Items starting index (from end of header)
 * [4] End of file index
 * Data itself
 */
class IndexedDeserializationImpl<T : Any>(val serialization: Serialization<T>) : IndexedDeserialization<T> {
    override fun deserialize(reader: SimpleReader): List<T> {
        val dataIndex = reader.readInt()
        val items = reader.readInt()
        reader.skip(dataIndex - 2 * IndexItemSize) // already consumed 8 bytes
        return serialization.ofList(items).deserialize(reader)
    }

    override fun deserialize(index: Int, reader: SimpleReader): T {
        var bytesRead = 2 * IndexItemSize
        val headerLength = reader.readInt()
        val items = reader.readInt()

        require(index >= 0 && index < items) { "Index is not valid. Index: $index, Items: $items" }

        val skippedBytes = IndexItemSize * index
        reader.skip(skippedBytes)
        bytesRead += skippedBytes

        val position = reader.readInt()
        bytesRead += IndexItemSize
        reader.skip(position + headerLength - bytesRead)

        return serialization.deserialize(reader)
    }

    override fun serialize(writer: SimpleWriter, data: List<T>) {
        val buffer = Buffer(ByteArrayOutputStream2(1048576))
        val indexes = IntArray(data.size)

        data.forEachIndexed { i, item ->
            indexes[i] = buffer.size
            serialization.serialize(buffer, item)
        }

        writer.writeInt(IndexItemSize * (3 + indexes.size))
        writer.writeInt(indexes.size)
        indexes.forEach { writer.writeInt(it) }
        writer.writeInt(buffer.size)
        writer.writeByteArray(buffer.getBuffer(), buffer.size)
    }
}

class ByteArrayOutputStream2(size: Int) : ByteArrayOutputStream(size) {
    /** Returns the internal buffer of this ByteArrayOutputStream, without copying.  */
    @Synchronized fun buf(): ByteArray {
        return this.buf
    }
}

class Buffer(val stream: ByteArrayOutputStream2, val writer: SimpleWriter = stream.simpleWriter()) : SimpleWriter by writer {
    override val size: Int
        get() = stream.size()

    fun reset() {
        stream.reset()
    }

    fun getBuffer(): ByteArray = stream.buf()
}

private const val IndexItemSize = 4


fun <T : Any> Serialization<T>.toIndexed(): IndexedDeserialization<T> = IndexedDeserializationImpl(this)
