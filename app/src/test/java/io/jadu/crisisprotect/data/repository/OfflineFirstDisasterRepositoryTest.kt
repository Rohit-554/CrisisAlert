package io.jadu.crisisprotect.data.repository

import io.jadu.crisisprotect.data.local.DisasterEventDao
import io.jadu.crisisprotect.data.local.DisasterEventEntity
import io.jadu.crisisprotect.data.remote.UsgsEarthquakeFeatureDto
import io.jadu.crisisprotect.data.remote.UsgsEarthquakeFeedDto
import io.jadu.crisisprotect.data.remote.UsgsEarthquakeGeometryDto
import io.jadu.crisisprotect.data.remote.UsgsEarthquakePropertiesDto
import io.jadu.crisisprotect.data.remote.UsgsEarthquakeService
import io.jadu.crisisprotect.domain.model.RefreshResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class OfflineFirstDisasterRepositoryTest {
    @Test
    fun `refresh stores valid records and ignores malformed records`() = runBlocking {
        val dao = FakeDao()
        val repository = OfflineFirstDisasterRepository(FakeService(validFeed()), dao)

        assertEquals(RefreshResult.Success, repository.refreshEvents())
        assertEquals(listOf("usgs:valid"), repository.observeEvents().first().map { it.id })
    }

    @Test
    fun `refresh failure retains existing cached records`() = runBlocking {
        val dao = FakeDao().apply { insertEvents(listOf(cachedEntity())) }
        val repository = OfflineFirstDisasterRepository(FakeService(error = IllegalStateException()), dao)

        assertEquals(RefreshResult.Failure, repository.refreshEvents())
        assertEquals(listOf("usgs:cached"), repository.observeEvents().first().map { it.id })
    }

    private fun validFeed() = UsgsEarthquakeFeedDto(
        features = listOf(
            UsgsEarthquakeFeatureDto(
                id = "valid",
                properties = UsgsEarthquakePropertiesDto(time = 1_700_000_000_000),
                geometry = UsgsEarthquakeGeometryDto(listOf(10.0, 20.0)),
            ),
            UsgsEarthquakeFeatureDto(id = "invalid"),
        ),
    )

    private fun cachedEntity() = DisasterEventEntity(
        id = "usgs:cached",
        title = "Cached",
        type = "EARTHQUAKE",
        latitude = 0.0,
        longitude = 0.0,
        locationName = null,
        occurredAtEpochMillis = 1,
        updatedAtEpochMillis = null,
        magnitude = null,
        source = "USGS",
        sourceUrl = null,
    )
}

private class FakeService(
    private val feed: UsgsEarthquakeFeedDto? = null,
    private val error: Throwable? = null,
) : UsgsEarthquakeService {
    override suspend fun getRecentEarthquakes(): UsgsEarthquakeFeedDto {
        error?.let { throw it }
        return requireNotNull(feed)
    }
}

private class FakeDao : DisasterEventDao {
    private val events = MutableStateFlow<List<DisasterEventEntity>>(emptyList())

    override fun observeEvents(): Flow<List<DisasterEventEntity>> = events

    override fun observeEvent(id: String): Flow<DisasterEventEntity?> =
        MutableStateFlow(events.value.firstOrNull { it.id == id })

    override suspend fun insertEvents(events: List<DisasterEventEntity>) {
        this.events.value = (this.events.value.filterNot { current -> events.any { it.id == current.id } } + events)
            .sortedByDescending { it.occurredAtEpochMillis }
    }

    override suspend fun deleteEventsForSource(source: String) {
        events.value = events.value.filterNot { it.source == source }
    }
}
