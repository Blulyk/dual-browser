package com.blulyk.dualbrowser.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        BookmarkEntity::class,
        HistoryEntity::class,
        SessionTabEntity::class,
        SessionMetaEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class BrowserDatabase : RoomDatabase() {
    abstract fun browserDao(): BrowserDao
}

