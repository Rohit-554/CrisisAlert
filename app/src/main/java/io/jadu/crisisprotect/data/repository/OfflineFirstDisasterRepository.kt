package io.jadu.crisisprotect.data.repository

import io.jadu.crisisprotect.data.local.DisasterEventDao
import io.jadu.crisisprotect.data.mapper.toDisasterEventOrNull
import io.jadu.crisisprotect.data.mapper.toDomain
import io.jadu.crisisprotect.data.mapper.toEntity
import io.jadu.crisisprotect.data.remote.UsgsEarthquakeService
import io.jadu.crisisprotect.domain.model.DisasterEvent
import io.jadu.crisisprotect.domain.model.RefreshResult
import io.jadu.crisisprotect.domain.repository.DisasterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex

private const val UsgsSource = "USGS"

class OfflineFirstDisasterRepository(
    private val service: UsgsEarthquakeService,
    private val dao: DisasterEventDao,
) : DisasterRepository {
    private val refreshMutex = Mutex()

    override fun observeEvents(): Flow<List<DisasterEvent>> =
        dao.observeEvents().map { entities -> entities.map { it.toDomain() } }

    override fun observeEvent(id: String): Flow<DisasterEvent?> =
        dao.observeEvent(id).map { entity -> entity?.toDomain() }

    override suspend fun refreshEvents(): RefreshResult {
        if (!refreshMutex.tryLock()) return RefreshResult.Success
        return try {
            val events = service.getRecentEarthquakes()
                .features
                .mapNotNull { it.toDisasterEventOrNull() }
                .map { it.toEntity() }
            dao.replaceEventsForSource(UsgsSource, events)
            RefreshResult.Success
        } catch (_: Exception) {
            RefreshResult.Failure
        } finally {
            refreshMutex.unlock()
        }
    }
}
