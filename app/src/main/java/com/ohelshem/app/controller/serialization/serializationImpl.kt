package com.ohelshem.app.controller.serialization

import java.io.*

class DataOutputStreamWriter(private val stream: DataOutputStream) : SimpleWriter {
    override val size: Int
        get() = stream.size()

    override fun writeInt(value: Int) = stream.writeInt(value)

    override fun writeByte(value: Int) = stream.writeByte(value)

    override fun writeByteArray(value: ByteArray, length: Int) = if (length == -1) stream.write(value) else stream.write(value, 0, length)

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

    override fun skip(bytes: Int) = stream.skipBytes(bytes)
}

fun File.simpleWriter(): SimpleWriter = DataOutputStreamWriter(DataOutputStream(outputStream()))
fun File.simpleReader(): SimpleReader = DataOutputStreamReader(DataInputStream(inputStream()))
fun OutputStream.simpleWriter(): SimpleWriter = DataOutputStreamWriter(DataOutputStream(this))
fun InputStream.simpleReader(): SimpleReader = DataOutputStreamReader(DataInputStream(this))
