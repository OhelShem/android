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
 * [4] <-> Items starting index
 * [4] End of file index
 * Data itself
 */
class IndexedDeserializationImpl<T : Any>(val serialization: Serialization<T>) : IndexedDeserialization<T> {
    override fun deserialize(reader: SimpleReader): List<T> {
        val dataIndex = reader.readInt()
        reader.skip(dataIndex - 4) // already consumed 4 bytes
        return serialization.ofList().deserialize(reader)
    }

    override fun deserialize(index: Int, reader: SimpleReader): T {
        var bytesRead = 2 * IndexItemSize
        reader.readInt()
        val items = reader.readInt()

        require(index >= 0 && index < items) { "Index is not valid. Index: $index, Items: $items" }

        val skippedBytes = IndexItemSize * index
        reader.skip(skippedBytes)
        bytesRead += skippedBytes

        val position = reader.readInt()
        reader.skip(position - bytesRead)

        return serialization.deserialize(reader)
    }

    override fun serialize(writer: SimpleWriter, data: List<T>) {
        val serializedDataStream = ByteArrayOutputStream2(1048576)
        val arrayWriter = serializedDataStream.simpleWriter()
        val indexes = IntArray(data.size)

        data.forEachIndexed { i, item ->
            indexes[i] = arrayWriter.size
            serialization.serialize(arrayWriter, item)
        }

        writer.writeInt(IndexItemSize * (3 + indexes.size))
        writer.writeInt(indexes.size)
        indexes.forEach { writer.writeInt(it) }
        writer.writeInt(arrayWriter.size)
        writer.writeByteArray(serializedDataStream.buf())
    }
}

class ByteArrayOutputStream2 : ByteArrayOutputStream {
    constructor() : super() {
    }

    constructor(size: Int) : super(size) {
    }

    /** Returns the internal buffer of this ByteArrayOutputStream, without copying.  */
    @Synchronized fun buf(): ByteArray {
        return this.buf
    }
}

private const val IndexItemSize = 4
