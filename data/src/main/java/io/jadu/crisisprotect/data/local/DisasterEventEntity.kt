package io.jadu.crisisprotect.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "disaster_events")
data class DisasterEventEntity(
    @PrimaryKey val id: String,
    val title: String?,
    val type: String,
    val latitude: Double?,
    val longitude: Double?,
    val locationName: String?,
    val occurredAtEpochMillis: Long,
    val updatedAtEpochMillis: Long?,
    val magnitude: Double?,
    val source: String,
    val sourceUrl: String?,
    val description: String? = null,
    val upstreamSource: String? = null,
    val upstreamSourceUrl: String? = null,
    val isSaved: Boolean = false,
)
