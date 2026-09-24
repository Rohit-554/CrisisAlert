package io.jadu.crisisprotect.data.mapper

import io.jadu.crisisprotect.data.remote.EonetEventFeatureDto
import io.jadu.crisisprotect.domain.model.DisasterEvent
import io.jadu.crisisprotect.domain.model.DisasterType
import java.time.Instant

private const val EonetSource = "EONET"

fun EonetEventFeatureDto.toDisasterEventOrNull(): DisasterEvent? {
    val rawId = id?.takeIf(String::isNotBlank) ?: return null
    val properties = properties ?: return null
    val geometry = geometry.firstOrNull() ?: return null
    val longitude = geometry.coordinates.getOrNull(0) ?: return null
    val latitude = geometry.coordinates.getOrNull(1) ?: return null
    if (longitude !in -180.0..180.0 || latitude !in -90.0..90.0) return null
    val occurredAt = geometry.date?.let { runCatching { Instant.parse(it) }.getOrNull() } ?: return null
    val type = properties.categories.firstNotNullOfOrNull { category ->
        when (category.id?.lowercase()) {
            "wildfires" -> DisasterType.WILDFIRE
            "severe-storms" -> DisasterType.STORM
            "volcanoes" -> DisasterType.VOLCANO
            "floods" -> DisasterType.FLOOD
            else -> null
        }
    } ?: return null
    val upstream = properties.sources.firstOrNull()
    return DisasterEvent(
        id = "eonet:$rawId", title = properties.title, description = properties.description,
        type = type, latitude = latitude, longitude = longitude, locationName = null,
        occurredAt = occurredAt, updatedAt = null, magnitude = null, source = EonetSource,
        sourceUrl = "https://eonet.gsfc.nasa.gov/api/v3/events/$rawId",
        upstreamSource = upstream?.id, upstreamSourceUrl = upstream?.url,
    )
}
