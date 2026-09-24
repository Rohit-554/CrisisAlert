package io.jadu.crisisprotect.domain.model

import java.time.Instant

data class DisasterEvent(
    val id: String,
    val title: String?,
    val type: DisasterType,
    val latitude: Double?,
    val longitude: Double?,
    val locationName: String?,
    val occurredAt: Instant,
    val updatedAt: Instant?,
    val magnitude: Double?,
    val source: String,
    val sourceUrl: String?,
    val description: String? = null,
    val upstreamSource: String? = null,
    val upstreamSourceUrl: String? = null,
    val isSaved: Boolean = false,
)

enum class DisasterType {
    EARTHQUAKE,
    WILDFIRE,
    STORM,
    VOLCANO,
    FLOOD,
    OTHER,
}
