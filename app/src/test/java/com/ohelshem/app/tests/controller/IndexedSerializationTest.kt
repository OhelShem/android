package com.ohelshem.app.tests.controller

import com.ohelshem.app.controller.serialization.*
import com.ohelshem.app.tests.getTestResource
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import com.ohelshem.api.model.Test as OHTest

class IndexedSerializationTest {
    @Test
    fun testCycleOfSimpleData() {
        val serialization = TestSerialization.toIndexed()

        val tests = List(10) { com.ohelshem.api.model.Test(it.toLong(), "$it") }
        val file = File.createTempFile("testCycleOfSimpleData", ".bin")

        file.simpleWriter().use { writer ->
            serialization.serialize(writer, tests)
        }

        file.simpleReader().use { reader ->
            val deserializedTests = serialization.deserialize(reader)
            assertEquals(tests, deserializedTests, "Tests are not equal")
        }

        file.simpleReader().use { reader ->
            val test3 = serialization.deserialize(3, reader)
            assertEquals(tests[3], test3, "Test in position 3 is not equal")
        }
    }

    @Test
    fun testCycleOfList() {
        val serialization = TestSerialization.ofList().toIndexed()

        val tests = List(10) { index -> List(10) { com.ohelshem.api.model.Test((index * 100 + it).toLong(), "${index * 100 + it}") } }
        val file = File.createTempFile("testCycleOfList", ".bin")

        file.simpleWriter().use { writer ->
            serialization.serialize(writer, tests)
        }

        file.simpleReader().use { reader ->
            val deserializedTests = serialization.deserialize(reader)
            assertEquals(tests, deserializedTests, "Tests are not equal")
        }

        file.simpleReader().use { reader ->
            val tests3 = serialization.deserialize(3, reader)
            assertEquals(tests[3], tests3, "Tests in position 3 are not equal")
        }
    }

    @Test
    fun testReadingRealData() {
        val file = "school_timetable_v5.bin".getTestResource()
        val serialization = SchoolHourSerialization.ofList().toIndexed()

        file.simpleReader().use { reader ->
            val allHours = serialization.deserialize(reader)
            assertEquals(48, allHours.size, "Size of all the hours is not 48 (12 classes * 4 layers)")
        }
    }

}