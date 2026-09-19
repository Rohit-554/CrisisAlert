package io.jadu.crisisprotect.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.jadu.crisisprotect.domain.model.DisasterEvent
import io.jadu.crisisprotect.domain.model.DisasterType
import io.jadu.crisisprotect.domain.model.RefreshResult
import io.jadu.crisisprotect.domain.repository.DisasterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: DisasterRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()
    private var cachedEvents: List<DisasterEvent> = emptyList()

    init {
        viewModelScope.launch {
            repository.observeEvents().collectLatest { events ->
                cachedEvents = events
                updateVisibleEvents()
            }
        }
        refresh()
    }

    fun selectFilter(filter: DisasterType?) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
        updateVisibleEvents()
    }

    fun refresh() {
        if (_uiState.value.isRefreshing) return
        _uiState.value = _uiState.value.copy(isRefreshing = true, refreshFailed = false)
        viewModelScope.launch {
            val result = repository.refreshEvents()
            _uiState.value = _uiState.value.copy(
                isInitialLoading = false,
                isRefreshing = false,
                refreshFailed = result is RefreshResult.Failure,
                hasLoadedOnce = true,
            )
            updateVisibleEvents()
        }
    }

    private fun updateVisibleEvents() {
        val selectedFilter = _uiState.value.selectedFilter
        val visibleEvents = cachedEvents.filter { event ->
            selectedFilter == null || event.type == selectedFilter
        }
        _uiState.value = _uiState.value.copy(events = visibleEvents)
    }
}
