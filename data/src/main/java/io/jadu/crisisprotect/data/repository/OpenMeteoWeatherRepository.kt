package io.jadu.crisisprotect.data.repository

import io.jadu.crisisprotect.data.remote.WeatherService
import io.jadu.crisisprotect.domain.model.CurrentWeather
import io.jadu.crisisprotect.domain.repository.WeatherRepository

class OpenMeteoWeatherRepository(private val service: WeatherService) : WeatherRepository {
    override suspend fun getCurrentWeather(latitude: Double, longitude: Double): Result<CurrentWeather> = runCatching {
        val weather = requireNotNull(service.getCurrentWeather(latitude, longitude).current)
        CurrentWeather(
            weather.temperatureCelsius, weather.humidityPercent, weather.precipitation,
            weather.weatherCode?.toReadableWeatherCondition(), weather.windSpeedKmh,
        )
    }
}

fun Int.toReadableWeatherCondition(): String = when (this) {
    0 -> "Clear sky"; 1, 2 -> "Partly cloudy"; 3 -> "Overcast"; 45, 48 -> "Fog"
    51, 53, 55, 56, 57 -> "Drizzle"; 61, 63, 65, 66, 67, 80, 81, 82 -> "Rain"
    71, 73, 75, 77, 85, 86 -> "Snow"; 95, 96, 99 -> "Thunderstorm"; else -> "Unknown conditions"
}
