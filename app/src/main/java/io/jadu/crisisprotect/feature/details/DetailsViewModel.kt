package io.jadu.crisisprotect.feature.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.jadu.crisisprotect.domain.model.DisasterEvent
import io.jadu.crisisprotect.domain.repository.DisasterRepository
import io.jadu.crisisprotect.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DetailsViewModel(
    private val eventId: String,
    private val repository: DisasterRepository,
    private val weatherRepository: WeatherRepository? = null,
    private val hapticController: EventHapticController? = null,
) : ViewModel() {
    private val _uiState = MutableStateFlow<DetailsUiState>(DetailsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeEvent(eventId).collectLatest { event ->
                hapticController?.stop()
                _uiState.value = event?.let { cachedEvent ->
                    DetailsUiState.Content(cachedEvent, haptics = cachedEvent.toHapticUiState(hapticController))
                } ?: DetailsUiState.Unavailable
                event?.let { cachedEvent ->
                    val latitude = cachedEvent.latitude
                    val longitude = cachedEvent.longitude
                    if (latitude != null && longitude != null && weatherRepository != null) loadWeather(latitude, longitude)
                }
            }
        }
    }

    fun toggleSaved() {
        val content = _uiState.value as? DetailsUiState.Content ?: return
        viewModelScope.launch { repository.setSaved(content.event.id, !content.event.isSaved) }
    }

    fun retryWeather() {
        val content = _uiState.value as? DetailsUiState.Content ?: return
        val latitude = content.event.latitude ?: return
        val longitude = content.event.longitude ?: return
        loadWeather(latitude, longitude)
    }

    fun toggleHapticExperience() {
        val content = _uiState.value as? DetailsUiState.Content ?: return
        if (content.haptics is EventHapticUiState.Playing) {
            stopHapticExperience()
            return
        }
        val durationMillis = hapticController?.play(content.event) ?: run {
            _uiState.value = content.copy(haptics = content.event.toHapticUiState(hapticController))
            return
        }
        _uiState.value = content.copy(haptics = EventHapticUiState.Playing)
        viewModelScope.launch {
            delay(durationMillis)
            val current = _uiState.value as? DetailsUiState.Content ?: return@launch
            if (current.event.id == content.event.id && current.haptics is EventHapticUiState.Playing) {
                _uiState.value = current.copy(haptics = current.event.toHapticUiState(hapticController))
            }
        }
    }

    private fun stopHapticExperience() {
        hapticController?.stop()
        val current = _uiState.value as? DetailsUiState.Content ?: return
        _uiState.value = current.copy(haptics = current.event.toHapticUiState(hapticController))
    }

    override fun onCleared() {
        hapticController?.stop()
        super.onCleared()
    }

    private fun loadWeather(latitude: Double, longitude: Double) {
        val weatherRepository = weatherRepository ?: return
        val content = _uiState.value as? DetailsUiState.Content ?: return
        if (content.weather is WeatherUiState.Loading || content.weather is WeatherUiState.Available) return
        _uiState.value = content.copy(weather = WeatherUiState.Loading)
        viewModelScope.launch {
            val weather = weatherRepository.getCurrentWeather(latitude, longitude)
            val current = _uiState.value as? DetailsUiState.Content ?: return@launch
            _uiState.value = current.copy(weather = weather.fold(WeatherUiState::Available) { WeatherUiState.Unavailable })
        }
    }
}

private fun DisasterEvent.toHapticUiState(controller: EventHapticController?): EventHapticUiState = when (controller?.availabilityFor(this)) {
    EventHapticAvailability.Supported -> EventHapticUiState.Ready
    EventHapticAvailability.Unavailable -> EventHapticUiState.Unavailable
    EventHapticAvailability.UnsupportedEvent, null -> EventHapticUiState.Unsupported
}
