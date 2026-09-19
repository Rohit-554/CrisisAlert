package io.jadu.crisisprotect.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [DisasterEventEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class CrisisProtectDatabase : RoomDatabase() {
    abstract fun disasterEventDao(): DisasterEventDao
}
