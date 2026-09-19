package io.jadu.crisisprotect.feature.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.jadu.crisisprotect.domain.repository.DisasterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DetailsViewModel(
    private val eventId: String,
    private val repository: DisasterRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<DetailsUiState>(DetailsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeEvent(eventId).collectLatest { event ->
                _uiState.value = event?.let(DetailsUiState::Content) ?: DetailsUiState.Unavailable
            }
        }
    }
}
