package io.jadu.crisisprotect.domain.repository

import io.jadu.crisisprotect.domain.model.DisasterEvent
import io.jadu.crisisprotect.domain.model.RefreshResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface DisasterRepository {
    fun observeEvents(): Flow<List<DisasterEvent>>

    fun observeEvent(id: String): Flow<DisasterEvent?>

    fun observeSavedEvents(): Flow<List<DisasterEvent>> = observeEvents().map { events -> events.filter(DisasterEvent::isSaved) }

    suspend fun refreshEvents(): RefreshResult

    suspend fun setSaved(eventId: String, isSaved: Boolean) = Unit
}
