package io.jadu.crisisprotect.feature.details

import io.jadu.crisisprotect.MainDispatcherRule
import io.jadu.crisisprotect.domain.model.DisasterEvent
import io.jadu.crisisprotect.domain.model.DisasterType
import io.jadu.crisisprotect.domain.model.RefreshResult
import io.jadu.crisisprotect.domain.repository.DisasterRepository
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `exposes cached event content`() = runTest {
        val event = testEvent()
        val viewModel = DetailsViewModel(event.id, DetailFakeRepository(event))

        advanceUntilIdle()

        assertEquals(event, (viewModel.uiState.value as DetailsUiState.Content).event)
    }

    @Test
    fun `exposes unavailable for a missing event`() = runTest {
        val viewModel = DetailsViewModel("missing", DetailFakeRepository(null))

        advanceUntilIdle()

        assertEquals(DetailsUiState.Unavailable, viewModel.uiState.value)
    }

    private fun testEvent() = DisasterEvent(
        id = "usgs:1",
        title = "Test event",
        type = DisasterType.EARTHQUAKE,
        latitude = 1.0,
        longitude = 2.0,
        locationName = "Test",
        occurredAt = Instant.ofEpochMilli(1),
        updatedAt = null,
        magnitude = 2.0,
        source = "USGS",
        sourceUrl = null,
    )
}

private class DetailFakeRepository(event: DisasterEvent?) : DisasterRepository {
    private val selectedEvent = MutableStateFlow(event)

    override fun observeEvents(): Flow<List<DisasterEvent>> = flowOf(selectedEvent.value?.let(::listOf).orEmpty())

    override fun observeEvent(id: String): Flow<DisasterEvent?> = selectedEvent

    override suspend fun refreshEvents(): RefreshResult = RefreshResult.Success
}
