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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
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

    @Test
    fun `invalid top-level response retains existing cached records`() = runBlocking {
        val dao = FakeDao().apply { insertEvents(listOf(cachedEntity())) }
        val repository = OfflineFirstDisasterRepository(
            FakeService(error = IllegalArgumentException("Invalid USGS feed")),
            dao,
        )

        assertEquals(RefreshResult.Failure, repository.refreshEvents())
        assertEquals(listOf("usgs:cached"), repository.observeEvents().first().map { it.id })
    }

    @Test
    fun `concurrent refreshes make only one source request`() = runTest {
        val service = BlockingService(validFeed())
        val repository = OfflineFirstDisasterRepository(service, FakeDao())

        val first = async { repository.refreshEvents() }
        service.awaitStarted()
        val second = async { repository.refreshEvents() }

        assertEquals(RefreshResult.Success, second.await())
        service.release()
        assertEquals(RefreshResult.Success, first.await())
        assertEquals(1, service.requests)
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

private class BlockingService(
    private val feed: UsgsEarthquakeFeedDto,
) : UsgsEarthquakeService {
    private val started = kotlinx.coroutines.CompletableDeferred<Unit>()
    private val release = kotlinx.coroutines.CompletableDeferred<Unit>()
    var requests = 0
        private set

    override suspend fun getRecentEarthquakes(): UsgsEarthquakeFeedDto {
        requests += 1
        started.complete(Unit)
        release.await()
        return feed
    }

    suspend fun awaitStarted() = started.await()

    fun release() = release.complete(Unit)
}

private class FakeDao : DisasterEventDao {
    private val events = MutableStateFlow<List<DisasterEventEntity>>(emptyList())

    override fun observeEvents(): Flow<List<DisasterEventEntity>> = events

    override fun observeEvent(id: String): Flow<DisasterEventEntity?> =
        MutableStateFlow(events.value.firstOrNull { it.id == id })

    override fun observeSavedEvents(): Flow<List<DisasterEventEntity>> =
        MutableStateFlow(events.value.filter { it.isSaved })

    override suspend fun savedIdsForSource(source: String): List<String> =
        events.value.filter { it.source == source && it.isSaved }.map { it.id }

    override suspend fun setSaved(id: String, isSaved: Boolean) {
        events.value = events.value.map { if (it.id == id) it.copy(isSaved = isSaved) else it }
    }

    override suspend fun insertEvents(events: List<DisasterEventEntity>) {
        this.events.value = (this.events.value.filterNot { current -> events.any { it.id == current.id } } + events)
            .sortedByDescending { it.occurredAtEpochMillis }
    }

    override suspend fun deleteEventsForSource(source: String) {
        events.value = events.value.filterNot { it.source == source }
    }
}
