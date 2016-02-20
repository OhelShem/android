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

package com.ohelshem.controller

import com.ohelshem.StubDBController
import com.ohelshem.api.model.Hour
import com.ohelshem.app.controller.DBController
import com.ohelshem.app.controller.TimetableController
import com.ohelshem.app.controller.TimetableControllerImpl
import org.junit.Before
import org.junit.Test
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.addSingleton
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class TimetableControllerTest {
    lateinit var timetableController: TimetableController

    @Before
    fun setUp() {
        Injekt.addSingleton<DBController>(TimetableTestDBController())
        timetableController = TimetableControllerImpl()
        timetableController.init()
    }

    @Test
    fun testDoesLearnOfFriday() {
        assertEquals(timetableController.learnsOnFriday, true)
    }

    @Test
    fun beforeSchool() {
        val hourData = timetableController.getHourData(day = 3, hour = 7, minutesNow = 27)
        assertTrue(hourData.isBefore)
        assertEquals(hourData.timeToHour, 33)
        assertEquals(hourData.hour, timetableController[day(3), hour(1)])
        assertEquals(hourData.nextHour, timetableController[day(3), hour(2)])
    }

    //region During school time
    @Test
    fun OnStartOfFirstLesson() {
        val hourData = timetableController.getHourData(day = 3, hour = 8, minutesNow = 0)
        assertTrue(!hourData.isBefore)
        assertEquals(hourData.timeToHour, 45)
        assertEquals(hourData.hour, timetableController[day(3), hour(1)])
        assertEquals(hourData.nextHour, timetableController[day(3), hour(2)])
    }

    @Test
    fun OnBottomOfLesson() {
        val hourData = timetableController.getHourData(day = 3, hour = 10, minutesNow = 18)
        assertTrue(!hourData.isBefore)
        assertEquals(hourData.timeToHour, 17)
        assertEquals(hourData.hour, timetableController[day(3), hour(3)])
        assertEquals(hourData.nextHour, timetableController[day(3), hour(4)])
    }

    @Test
    fun OnEndOfLesson() {
        val hourData = timetableController.getHourData(day = 3, hour = 10, minutesNow = 35)
        assertTrue(hourData.isBefore)
        assertEquals(hourData.timeToHour, 5)
        assertEquals(hourData.hour, timetableController[day(3), hour(4)])
        assertEquals(hourData.nextHour, timetableController[day(3), hour(5)])
    }

    @Test
    fun OnBreak() {
        val hourData = timetableController.getHourData(day = 3, hour = 10, minutesNow = 38)
        assertTrue(hourData.isBefore)
        assertEquals(hourData.timeToHour, 2)
        assertEquals(hourData.hour, timetableController[day(3), hour(4)])
        assertEquals(hourData.nextHour, timetableController[day(3), hour(5)])
    }

    @Test
    fun OnLastHour() {
        val hourData = timetableController.getHourData(day = 3, hour = 16, minutesNow = 25)
        assertTrue(!hourData.isBefore)
        assertEquals(hourData.timeToHour, 20)
        assertEquals(hourData.hour, timetableController[day(3), hour(10)])
        assertEquals(hourData.nextHour, Hour())
    }

    @Test
    fun OnLastMinute() {
        val hourData = timetableController.getHourData(day = 3, hour = 16, minutesNow = 45)
        assertTrue(hourData.isBefore)
        assertEquals(hourData.hour, timetableController[day(4), hour(1)])
        assertEquals(hourData.nextHour, timetableController[day(4), hour(2)])
    }
    //endregion

    //region After school
    @Test
    fun AfterSchool() {
        val hourData = timetableController.getHourData(day = 3, hour = 17, minutesNow = 50)
        assertTrue(hourData.isBefore)
        assertEquals(hourData.hour, timetableController[day(4), hour(1)])
        assertEquals(hourData.nextHour, timetableController[day(4), hour(2)])
    }

    @Test
    fun AfterSchoolOnThursday() {
        val hourData = timetableController.getHourData(day = 5, hour = 19, minutesNow = 42)
        assertTrue(hourData.isBefore)
        assertEquals(hourData.hour, timetableController[day(6), hour(1)])
        assertEquals(hourData.nextHour, timetableController[day(6), hour(2)])
    }

    @Test
    fun AfterSchoolOnFriday() {
        val hourData = timetableController.getHourData(day = 6, hour = 20, minutesNow = 17)
        assertTrue(hourData.isBefore)
        assertEquals(hourData.hour, timetableController[day(1), hour(1)])
        assertEquals(hourData.nextHour, timetableController[day(1), hour(2)])
    }

    //endregion

    @Test
    fun onFriday() {
        val hourData = timetableController.getHourData(day = 6, hour = 20, minutesNow = 50)
        assertTrue(hourData.isBefore)
        assertEquals(hourData.hour, timetableController[day(1), hour(1)])
        assertEquals(hourData.nextHour, timetableController[day(1), hour(2)])
    }

    @Test
    fun onSaturday() {
        val hourData = timetableController.getHourData(day = 7, hour = 14, minutesNow = 20)
        assertTrue(hourData.isBefore)
        assertEquals(hourData.hour, timetableController[day(1), hour(1)])
        assertEquals(hourData.nextHour, timetableController[day(1), hour(2)])
    }

    fun hour(hour: Int): Int = hour - 1

    fun day(day: Int): Int = day - 1

    open class TimetableTestDBController : StubDBController() {
        override var timetable: Array<Array<Hour>>? = Array(6) { day -> Array(10) { hour -> Hour("$day", "$hour") } }
    }
}