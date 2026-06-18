package com.blulyk.dualbrowser.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface BrowserDao {
    @Insert
    suspend fun insertHistory(entry: HistoryEntity)

    @Query("SELECT * FROM history ORDER BY visitedAt DESC, id DESC")
    fun observeHistory(): Flow<List<HistoryEntity>>

    @Query("DELETE FROM history")
    suspend fun clearHistory()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBookmark(bookmark: BookmarkEntity)

    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun observeBookmarks(): Flow<List<BookmarkEntity>>

    @Query("DELETE FROM bookmarks WHERE url = :url")
    suspend fun removeBookmark(url: String)

    @Query("DELETE FROM session_tabs")
    suspend fun clearSessionTabs()

    @Insert
    suspend fun insertSessionTabs(tabs: List<SessionTabEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSessionMeta(meta: SessionMetaEntity)

    @Query("SELECT * FROM session_tabs ORDER BY position")
    suspend fun readSessionTabs(): List<SessionTabEntity>

    @Query("SELECT * FROM session_meta WHERE `key` = 0")
    suspend fun readSessionMeta(): SessionMetaEntity?

    @Transaction
    suspend fun replaceSession(tabs: List<SessionTabEntity>, meta: SessionMetaEntity) {
        clearSessionTabs()
        if (tabs.isNotEmpty()) insertSessionTabs(tabs)
        upsertSessionMeta(meta)
    }
}

