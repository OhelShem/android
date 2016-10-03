/*
 * Copyright 2016 Yoav Sternberg.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.ohelshem.app.controller

import com.ohelshem.app.controller.utils.OffsetDataController
import com.ohelshem.app.controller.utils.OffsetDataControllerImpl
import com.ohelshem.app.getTestResource
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OffsetDataControllerTest {
    val controller: OffsetDataController = OffsetDataControllerImpl
    val OffsetFile = "timetable_offsets.bin".getTestResource()
    val DataFile = "timetable.bin".getTestResource()

    @Test
    fun testReadSpecific() {
        val data = controller.read(OffsetFile, DataFile, 12 + 12 + (4 - 1)).map(String::trim).normalize()
        val supposedData = "timetable_11_4_data.dsv".getTestResource().readLines().map(String::trim)
        assertEquals(supposedData.size, data.size)
        assertEquals(supposedData, data)
    }

    @Test
    fun testReadSpecific2() {
        val data = controller.read(OffsetFile, DataFile, 12 + 12 + 12 + (7 - 1)).normalize()
        val supposedData = "timetable_12_7_data.dsv".getTestResource().readLines()
        assertEquals(supposedData.size, data.size)
        assertTrue(data.zip(supposedData).any { !it.first.contentEquals(it.second) })
    }

    @Test
    fun testReadSpecific3() {
        val data = controller.read(OffsetFile, DataFile, 12 + (4 - 1))
        val supposedData = "timetable_10_4_data.dsv".getTestResource().readLines().normalize()
        assertEquals(supposedData.size, data.size)
        assertTrue(data.zip(supposedData).any { !it.first.contentEquals(it.second) })
    }

    @Test
    fun testReadAll() {
        val data = controller.read(OffsetFile, DataFile, OffsetDataController.AllFile).normalize()
        val supposedData = "timetable_all_data.dsv".getTestResource().readLines()
        assertEquals(supposedData.size, data.size)
        assertTrue(data.zip(supposedData).any { !it.first.contentEquals(it.second) })
    }

    fun List<String>.normalize() = map { it.replace('\u2004', ' ') }
}