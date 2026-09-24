package io.jadu.crisisprotect.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface DisasterEventDao {
    @Query("SELECT * FROM disaster_events ORDER BY occurredAtEpochMillis DESC")
    fun observeEvents(): Flow<List<DisasterEventEntity>>

    @Query("SELECT * FROM disaster_events WHERE id = :id LIMIT 1")
    fun observeEvent(id: String): Flow<DisasterEventEntity?>

    @Query("SELECT * FROM disaster_events WHERE isSaved = 1 ORDER BY occurredAtEpochMillis DESC")
    fun observeSavedEvents(): Flow<List<DisasterEventEntity>>

    @Query("SELECT id FROM disaster_events WHERE source = :source AND isSaved = 1")
    suspend fun savedIdsForSource(source: String): List<String>

    @Query("UPDATE disaster_events SET isSaved = :isSaved WHERE id = :id")
    suspend fun setSaved(id: String, isSaved: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<DisasterEventEntity>)

    @Query("DELETE FROM disaster_events WHERE source = :source AND isSaved = 0")
    suspend fun deleteEventsForSource(source: String)

    @Transaction
    suspend fun replaceEventsForSource(source: String, events: List<DisasterEventEntity>) {
        val savedIds = savedIdsForSource(source).toSet()
        deleteEventsForSource(source)
        if (events.isNotEmpty()) {
            insertEvents(events.map { event -> event.copy(isSaved = event.isSaved || event.id in savedIds) })
        }
    }
}
