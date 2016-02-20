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
import com.ohelshem.app.controller.BaseTimetableController
import com.ohelshem.app.controller.DBController
import com.ohelshem.app.controller.TimetableControllerImpl
import com.ohelshem.app.controller.TimetableControllerWrapper
import com.ohelshem.app.model.OverrideData
import org.junit.Before
import org.junit.Test
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.InjektScope
import uy.kohesive.injekt.api.addSingleton
import uy.kohesive.injekt.registry.default.DefaultRegistrar
import java.util.*
import kotlin.test.assertEquals

class TimetableControllerWrapperTest {
    lateinit var timetableController: BaseTimetableController
    private val supposedData: Array<Array<Hour>> = generateData()

    @Before
    fun setUp() {
        Injekt = InjektScope(DefaultRegistrar())
        Injekt.addSingleton<DBController>(TimetableControllerWrapperTestDBController)
        timetableController = TimetableControllerImpl()
        timetableController.colors = intArrayOf(0, 1, 2, 3)
        timetableController.init()
    }

    @Test
    fun testWorkingRegular() {
        using { timetableWrapper ->
            fakeOverrides = mutableListOf()
            notifyListeners()
            assertEquals(timetableWrapper.size, 6)
            for (day in 0..5) {
                for (hour in 0..timetableWrapper[day].size - 1) {
                    assertEquals(supposedData[day][hour], timetableWrapper[day, hour], "Day $day :: Hour $hour")
                }
            }
        }
    }

    @Test
    fun testOverrides() {
        using { timetableWrapper ->
            val overrideName = "An override"
            fakeOverrides = mutableListOf(OverrideData(3, 1, overrideName, "")) // wednesday, the forth hour
            notifyListeners()
            assertEquals(timetableWrapper.size, 6)
            for (day in 0..5) {
                for (hour in 0..timetableWrapper[day].size - 1) {
                    if (day != 3 || hour != 1)
                        assertEquals(supposedData[day][hour], timetableWrapper[day, hour], "Day $day :: Hour $hour")
                }
            }
            assertEquals(overrideName, timetableWrapper[3, 1].name)
        }
    }

    @Test
    fun testOverrides2() {
        using { timetableWrapper ->
            val overrideName = "An override"
            fakeOverrides = mutableListOf(OverrideData(3, 1, overrideName, ""), OverrideData(3, 3, overrideName, ""), OverrideData(3, 5, overrideName, ""))
            notifyListeners()
            assertEquals(timetableWrapper.size, 6)
            for (day in 0..5) {
                for (hour in 0..timetableWrapper[day].size - 1) {
                    if (day != 3 || (hour != 1 && hour != 3 && hour != 5))
                        assertEquals(supposedData[day][hour], timetableWrapper[day, hour], "Day $day :: Hour $hour")
                }
            }
            assertEquals(overrideName, timetableWrapper[3, 1].name)
            assertEquals(overrideName, timetableWrapper[3, 3].name)
            assertEquals(overrideName, timetableWrapper[3, 5].name)

        }
    }


    private inline fun using(operation: (TimetableControllerWrapper) -> Unit) {
        val timetable = TimetableControllerWrapper(timetableController)
        operation(timetable)
        TimetableControllerWrapperTestDBController.overridesListeners.clear()
    }


    object TimetableControllerWrapperTestDBController : StubDBController() {
        override var overrides: Array<OverrideData>
            get() = fakeOverrides.toTypedArray()
            set(value) {
            }

        override var timetable: Array<Array<Hour>>? = generateData()
    }


    companion object {
        private var fakeOverrides: MutableList<OverrideData> = LinkedList()
        private fun notifyListeners() = TimetableControllerWrapperTestDBController.overridesListeners.values.forEach { it.onUpdate(fakeOverrides.toTypedArray()) }
        private fun generateData() = Array(6) { day -> Array(10) { hour -> Hour("$day", "$hour") } }
    }
}