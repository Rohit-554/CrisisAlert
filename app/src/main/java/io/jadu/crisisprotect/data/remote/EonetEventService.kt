package io.jadu.crisisprotect.data.remote

import retrofit2.http.GET

interface EonetEventService {
    @GET("api/v3/events/geojson")
    suspend fun getRecentEvents(): EonetEventFeedDto
}
