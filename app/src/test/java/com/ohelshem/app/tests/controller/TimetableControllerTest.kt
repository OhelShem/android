package com.ohelshem.app.tests.controller

import com.ohelshem.api.model.Hour
import com.ohelshem.app.controller.timetable.BaseTimetableController
import com.ohelshem.app.controller.timetable.TimetableController
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class TimetableControllerTest {
    private lateinit var timetableController: TimetableController

    @Before
    fun setUp() {
        timetableController = TimetableControllerImpl()
        timetableController.init()
    }

    @Test
    fun testDoesLearnOfFriday() = assertEquals(timetableController.learnsOnFriday, true)

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
    fun InMiddleOfLesson() {
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
    fun InLastHour() {
        val hourData = timetableController.getHourData(day = 3, hour = 17, minutesNow = 23)
        assertTrue(!hourData.isBefore)
        assertEquals(hourData.hour, timetableController[day(3), hour(11)])
        assertEquals(hourData.nextHour, timetableController[day(4), hour(1)])
    }

    @Test
    fun InLastHourOfWeek() {
        val hourData = timetableController.getHourData(day = 6, hour = 17, minutesNow = 23)
        assertTrue(!hourData.isBefore)
        assertEquals(hourData.hour, timetableController[day(6), hour(11)])
        assertEquals(hourData.nextHour, timetableController[day(1), hour(1)])
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
    fun onSaturday() {
        val hourData = timetableController.getHourData(day = 7, hour = 14, minutesNow = 20)
        assertTrue(hourData.isBefore)
        assertEquals(hourData.hour, timetableController[day(1), hour(1)])
        assertEquals(hourData.nextHour, timetableController[day(1), hour(2)])
    }

    private fun hour(hour: Int): Int = hour - 1

    private fun day(day: Int): Int = day - 1

    private class TimetableControllerImpl : BaseTimetableController() {
        override fun init() {
            timetable = Array(6) { day -> Array(11) { hour -> Hour("day: ${day + 1}", "hour: ${hour + 1}") } }
        }
    }

}