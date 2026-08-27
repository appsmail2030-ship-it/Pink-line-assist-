package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.AssistanceType
import com.example.data.model.RequestStatus
import com.example.data.model.Station
import com.example.data.repository.PinkLineRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var repository: PinkLineRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = AppDatabase.getDatabase(context)
        repository = PinkLineRepository(database)
    }

    @Test
    fun `read string from context matches app name`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("Pink Line Assist", appName)
    }

    @Test
    fun `test station count calculation`() = runBlocking {
        // Anand Vihar (28) to Mayur Vihar (22) = 6 stations
        val count = repository.calculateStationCount("ST_28", "ST_22")
        assertEquals(6, count)
    }

    @Test
    fun `test ETA calculation`() = runBlocking {
        // 6 stations * 130s = 780s
        val (seconds, _) = repository.calculateETA(6)
        assertEquals(780L, seconds)
    }

    @Test
    fun `test create assistance request`() = runBlocking {
        val req = repository.createRequest(
            sourceStationId = "ST_28",
            destinationStationId = "ST_22",
            trainId = "T-245",
            passengerCount = 2,
            assistanceType = AssistanceType.WHEELCHAIR.label,
            wheelchairRequired = true,
            circularDirection = "Platform 1",
            createdBy = "Rajesh Sharma",
            remarks = "Wheelchair ramp needed at Mayur Vihar"
        )

        assertNotNull(req)
        assertTrue(req.requestId.startsWith("PM-"))
        assertEquals("REQUESTED", req.status)
        assertEquals(2, req.passengerCount)
    }
}
