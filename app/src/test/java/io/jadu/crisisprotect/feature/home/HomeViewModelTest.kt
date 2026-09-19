package io.jadu.crisisprotect.feature.home

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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `keeps cached events visible after refresh failure`() = runTest {
        val repository = FakeDisasterRepository(listOf(event("1"))).apply {
            refreshResult = RefreshResult.Failure
        }
        val viewModel = HomeViewModel(repository)

        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.events.size)
        assertTrue(viewModel.uiState.value.refreshFailed)
        assertFalse(viewModel.uiState.value.isInitialLoading)
    }

    @Test
    fun `filters events locally`() = runTest {
        val repository = FakeDisasterRepository(listOf(event("1"), event("2", DisasterType.OTHER)))
        val viewModel = HomeViewModel(repository)

        advanceUntilIdle()
        viewModel.selectFilter(DisasterType.EARTHQUAKE)

        assertEquals(listOf("1"), viewModel.uiState.value.events.map { it.id })
    }

    @Test
    fun `shows recoverable initial failure and clears it after retry`() = runTest {
        val repository = FakeDisasterRepository(emptyList()).apply { refreshResult = RefreshResult.Failure }
        val viewModel = HomeViewModel(repository)

        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.refreshFailed)
        assertTrue(viewModel.uiState.value.events.isEmpty())

        repository.refreshResult = RefreshResult.Success
        viewModel.refresh()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.refreshFailed)
        assertFalse(viewModel.uiState.value.isInitialLoading)
    }

    private fun event(id: String, type: DisasterType = DisasterType.EARTHQUAKE) = DisasterEvent(
        id = id,
        title = "Event $id",
        type = type,
        latitude = 0.0,
        longitude = 0.0,
        locationName = "Location",
        occurredAt = Instant.ofEpochMilli(1_700_000_000_000),
        updatedAt = null,
        magnitude = 4.0,
        source = "USGS",
        sourceUrl = null,
    )
}

private class FakeDisasterRepository(initialEvents: List<DisasterEvent>) : DisasterRepository {
    private val events = MutableStateFlow(initialEvents)
    var refreshResult: RefreshResult = RefreshResult.Success

    override fun observeEvents(): Flow<List<DisasterEvent>> = events

    override fun observeEvent(id: String): Flow<DisasterEvent?> = flowOf(events.value.firstOrNull { it.id == id })

    override suspend fun refreshEvents(): RefreshResult = refreshResult
}
