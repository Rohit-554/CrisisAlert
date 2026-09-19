package io.jadu.crisisprotect.domain.repository

import io.jadu.crisisprotect.domain.model.DisasterEvent
import io.jadu.crisisprotect.domain.model.RefreshResult
import kotlinx.coroutines.flow.Flow

interface DisasterRepository {
    fun observeEvents(): Flow<List<DisasterEvent>>

    fun observeEvent(id: String): Flow<DisasterEvent?>

    suspend fun refreshEvents(): RefreshResult
}
