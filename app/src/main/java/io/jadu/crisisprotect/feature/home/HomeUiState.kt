package io.jadu.crisisprotect.feature.home

import io.jadu.crisisprotect.domain.model.DisasterEvent
import io.jadu.crisisprotect.domain.model.DisasterType

data class HomeUiState(
    val events: List<DisasterEvent> = emptyList(),
    val selectedFilter: DisasterType? = null,
    val isInitialLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val refreshFailed: Boolean = false,
    val hasLoadedOnce: Boolean = false,
)
