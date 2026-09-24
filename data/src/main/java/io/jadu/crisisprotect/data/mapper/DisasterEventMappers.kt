package io.jadu.crisisprotect.data.mapper

import io.jadu.crisisprotect.data.local.DisasterEventEntity
import io.jadu.crisisprotect.data.remote.UsgsEarthquakeFeatureDto
import io.jadu.crisisprotect.domain.model.DisasterEvent
import io.jadu.crisisprotect.domain.model.DisasterType
import java.time.Instant

private const val UsgsSource = "USGS"

fun UsgsEarthquakeFeatureDto.toDisasterEventOrNull(): DisasterEvent? {
    val rawId = id?.takeIf { it.isNotBlank() } ?: return null
    val eventProperties = properties ?: return null
    val occurredAt = eventProperties.time?.let(Instant::ofEpochMilli) ?: return null
    val coordinates = geometry?.coordinates ?: return null
    val longitude = coordinates.getOrNull(0) ?: return null
    val latitude = coordinates.getOrNull(1) ?: return null
    if (longitude !in -180.0..180.0 || latitude !in -90.0..90.0) return null

    return DisasterEvent(
        id = "usgs:$rawId",
        title = eventProperties.title,
        type = DisasterType.EARTHQUAKE,
        latitude = latitude,
        longitude = longitude,
        locationName = eventProperties.place,
        occurredAt = occurredAt,
        updatedAt = eventProperties.updated?.let(Instant::ofEpochMilli),
        magnitude = eventProperties.mag,
        source = UsgsSource,
        sourceUrl = eventProperties.url,
    )
}

fun DisasterEvent.toEntity(): DisasterEventEntity = DisasterEventEntity(
    id = id,
    title = title,
    type = type.name,
    latitude = latitude,
    longitude = longitude,
    locationName = locationName,
    occurredAtEpochMillis = occurredAt.toEpochMilli(),
    updatedAtEpochMillis = updatedAt?.toEpochMilli(),
    magnitude = magnitude,
    source = source,
    sourceUrl = sourceUrl,
    description = description,
    upstreamSource = upstreamSource,
    upstreamSourceUrl = upstreamSourceUrl,
    isSaved = isSaved,
)

fun DisasterEventEntity.toDomain(): DisasterEvent = DisasterEvent(
    id = id,
    title = title,
    type = DisasterType.entries.firstOrNull { it.name == type } ?: DisasterType.OTHER,
    latitude = latitude,
    longitude = longitude,
    locationName = locationName,
    occurredAt = Instant.ofEpochMilli(occurredAtEpochMillis),
    updatedAt = updatedAtEpochMillis?.let(Instant::ofEpochMilli),
    magnitude = magnitude,
    source = source,
    sourceUrl = sourceUrl,
    description = description,
    upstreamSource = upstreamSource,
    upstreamSourceUrl = upstreamSourceUrl,
    isSaved = isSaved,
)
