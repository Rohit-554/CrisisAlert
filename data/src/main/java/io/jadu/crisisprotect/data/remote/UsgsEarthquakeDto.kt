package io.jadu.crisisprotect.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class UsgsEarthquakeFeedDto(
    val features: List<UsgsEarthquakeFeatureDto> = emptyList(),
)

@Serializable
data class UsgsEarthquakeFeatureDto(
    val id: String? = null,
    val properties: UsgsEarthquakePropertiesDto? = null,
    val geometry: UsgsEarthquakeGeometryDto? = null,
)

@Serializable
data class UsgsEarthquakePropertiesDto(
    val title: String? = null,
    val place: String? = null,
    val mag: Double? = null,
    val time: Long? = null,
    val updated: Long? = null,
    val url: String? = null,
    val type: String? = null,
)

@Serializable
data class UsgsEarthquakeGeometryDto(
    val coordinates: List<Double?> = emptyList(),
)
