package io.jadu.crisisprotect.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class EonetEventFeedDto(val features: List<EonetEventFeatureDto> = emptyList())

@Serializable
data class EonetEventFeatureDto(
    val id: String? = null,
    val properties: EonetEventPropertiesDto? = null,
    val geometry: List<EonetEventGeometryDto> = emptyList(),
)

@Serializable
data class EonetEventPropertiesDto(
    val title: String? = null,
    val description: String? = null,
    val categories: List<EonetCategoryDto> = emptyList(),
    val sources: List<EonetSourceDto> = emptyList(),
)

@Serializable data class EonetCategoryDto(val id: String? = null, val title: String? = null)
@Serializable data class EonetSourceDto(val id: String? = null, val url: String? = null)
@Serializable data class EonetEventGeometryDto(val date: String? = null, val coordinates: List<Double?> = emptyList())
