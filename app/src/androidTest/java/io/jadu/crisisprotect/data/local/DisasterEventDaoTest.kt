package io.jadu.crisisprotect.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DisasterEventDaoTest {
    private lateinit var database: CrisisProtectDatabase
    private lateinit var dao: DisasterEventDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            CrisisProtectDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = database.disasterEventDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun replacement_updates_source_snapshot_in_descending_order() = runBlocking {
        dao.replaceEventsForSource("USGS", listOf(entity("old", 1)))
        dao.replaceEventsForSource("USGS", listOf(entity("newer", 3), entity("new", 2)))

        assertEquals(listOf("newer", "new"), dao.observeEvents().first().map { it.id })
    }

    private fun entity(id: String, occurredAt: Long) = DisasterEventEntity(
        id = id,
        title = id,
        type = "EARTHQUAKE",
        latitude = 0.0,
        longitude = 0.0,
        locationName = null,
        occurredAtEpochMillis = occurredAt,
        updatedAtEpochMillis = null,
        magnitude = null,
        source = "USGS",
        sourceUrl = null,
    )
}
