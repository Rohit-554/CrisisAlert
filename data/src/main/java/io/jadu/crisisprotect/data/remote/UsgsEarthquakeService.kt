package io.jadu.crisisprotect.data.remote

import retrofit2.http.GET

interface UsgsEarthquakeService {
    @GET("earthquakes/feed/v1.0/summary/2.5_day.geojson")
    suspend fun getRecentEarthquakes(): UsgsEarthquakeFeedDto
}
