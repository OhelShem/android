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

package com.ohelshem.app.controller

import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream

/**
 * An interface for classes that their functionality is reading from offset files.
 *
 * Offset files are the format used by this app for storing long data,
 * instead of json or xml. It is based on 2 files:
 *
 * * The offset table - a table of binary ints.
 * * The data - string table separated by [Separator],
 *
 * For getting the data positioned as `N`, we need to read the `N` and `N+1` ints from
 * the offset table. Then, we need to read the bytes start at the first value, and
 * end on the seconds value. Then, splitting by the [Separator] .
 */
interface OffsetDataController {
    /**
     * Read the data from the files and return the data for the [position]
     *
     */
    fun read(offsetFile: File, dataFile: File, position: Int): List<String> = read(DataInputStream(FileInputStream(offsetFile)), DataInputStream(FileInputStream(dataFile)), position)

    /**
     * Read the data from the streams and return the data for the [position]
     *
     * **Note:** the method should close the streams.
     */
    fun read(offsetFileStream: DataInputStream, dataFileStream: DataInputStream, position: Int): List<String>

    /**
     * Write the [data] to file.
     */
    fun write(offsetFile: File, dataFile: File, data: List<String>)


    companion object {
        internal const val Separator = '|'
        internal val SeparatorBytes = Separator.toString().toByteArray()
        internal const val OffsetDataSize: Int = 4
        const val AllFile: Int = -1
    }
}