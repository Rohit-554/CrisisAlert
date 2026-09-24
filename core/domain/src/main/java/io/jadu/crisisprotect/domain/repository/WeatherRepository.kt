package io.jadu.crisisprotect.domain.repository

import io.jadu.crisisprotect.domain.model.CurrentWeather

interface WeatherRepository {
    suspend fun getCurrentWeather(latitude: Double, longitude: Double): Result<CurrentWeather>
}
