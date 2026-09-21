package io.jadu.crisisprotect.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponseDto(@SerialName("current") val current: CurrentWeatherDto? = null)

@Serializable
data class CurrentWeatherDto(
    @SerialName("temperature_2m") val temperatureCelsius: Double? = null,
    @SerialName("relative_humidity_2m") val humidityPercent: Int? = null,
    val precipitation: Double? = null,
    @SerialName("weather_code") val weatherCode: Int? = null,
    @SerialName("wind_speed_10m") val windSpeedKmh: Double? = null,
)
