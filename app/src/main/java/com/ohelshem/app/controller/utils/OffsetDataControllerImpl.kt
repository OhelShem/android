/*
 * Copyright 2016 Yoav Sternberg.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.ohelshem.app.controller.utils

import java.io.*

/**
 * The default [OffsetDataController].
 */
object OffsetDataControllerImpl : OffsetDataController {
    override fun read(offsetFileStream: DataInputStream, dataFileStream: DataInputStream, position: Int): List<String> {
        val offset: Int
        var nextOffset = 0
        if (position != OffsetDataController.AllFile) {
            offsetFileStream.skip((position * OffsetDataController.OffsetDataSize).toLong())
            offset = offsetFileStream.readInt()
            nextOffset = offsetFileStream.readInt() // we always has next, because for the last data we got file size after it.
        } else {
            offset = offsetFileStream.readInt()
            while (true) {
                try {
                    nextOffset = offsetFileStream.readInt()
                } catch(ignored: EOFException) {
                    break
                }
            }
        }
        offsetFileStream.close()
        if (offset == nextOffset) return emptyList()

        dataFileStream.use {
            val buffer = readBytes(it, offset, nextOffset)
            return String(buffer, Charsets.UTF_8).split(OffsetDataController.Separator)
        }
    }

    private fun readBytes(dataFileStream: DataInputStream, offset: Int, nextOffset: Int): ByteArray {
        dataFileStream.skip(offset.toLong())
        val buffer = ByteArray(nextOffset - offset - 1)
        dataFileStream.read(buffer, 0, buffer.size)
        return buffer
    }

    override fun write(offsetFile: File, dataFile: File, data: Sequence<String>) {
        offsetFile.createNewFile()
        dataFile.createNewFile()

        val dataWriter = DataOutputStream(FileOutputStream(dataFile))
        val offsetWriter = DataOutputStream(FileOutputStream(offsetFile))
        var offsetFromStart = 0
        // For each layer:
        data.forEach {
            offsetWriter.writeInt(offsetFromStart)
            val bytes = it.toByteArray()
            dataWriter.write(bytes)
            dataWriter.write(OffsetDataController.SeparatorBytes)
            offsetFromStart += (bytes.size + OffsetDataController.SeparatorBytes.size)
        }
        offsetWriter.writeInt(offsetFromStart)

        dataWriter.close()
        offsetWriter.close()
    }
}