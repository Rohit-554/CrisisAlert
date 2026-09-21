package io.jadu.crisisprotect.domain.model

data class CurrentWeather(
    val temperatureCelsius: Double?,
    val humidityPercent: Int?,
    val precipitationMm: Double?,
    val condition: String?,
    val windSpeedKmh: Double?,
)
