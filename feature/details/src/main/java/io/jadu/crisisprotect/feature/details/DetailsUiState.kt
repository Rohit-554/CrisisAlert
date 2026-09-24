package io.jadu.crisisprotect.feature.details

import io.jadu.crisisprotect.domain.model.DisasterEvent
import io.jadu.crisisprotect.domain.model.CurrentWeather

sealed interface DetailsUiState {
    data object Loading : DetailsUiState
    data class Content(
        val event: DisasterEvent,
        val weather: WeatherUiState = WeatherUiState.Idle,
        val haptics: EventHapticUiState = EventHapticUiState.Unsupported,
    ) : DetailsUiState
    data object Unavailable : DetailsUiState
}

sealed interface EventHapticUiState {
    data object Unsupported : EventHapticUiState
    data object Ready : EventHapticUiState
    data object Playing : EventHapticUiState
    data object Unavailable : EventHapticUiState
}

sealed interface WeatherUiState {
    data object Idle : WeatherUiState
    data object Loading : WeatherUiState
    data class Available(val weather: CurrentWeather) : WeatherUiState
    data object Unavailable : WeatherUiState
}
