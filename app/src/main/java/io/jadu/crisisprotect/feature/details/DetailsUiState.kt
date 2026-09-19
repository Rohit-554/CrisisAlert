package io.jadu.crisisprotect.feature.details

import io.jadu.crisisprotect.domain.model.DisasterEvent

sealed interface DetailsUiState {
    data object Loading : DetailsUiState
    data class Content(val event: DisasterEvent) : DetailsUiState
    data object Unavailable : DetailsUiState
}
