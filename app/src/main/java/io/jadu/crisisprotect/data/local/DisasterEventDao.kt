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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<DisasterEventEntity>)

    @Query("DELETE FROM disaster_events WHERE source = :source")
    suspend fun deleteEventsForSource(source: String)

    @Transaction
    suspend fun replaceEventsForSource(source: String, events: List<DisasterEventEntity>) {
        deleteEventsForSource(source)
        if (events.isNotEmpty()) {
            insertEvents(events)
        }
    }
}
