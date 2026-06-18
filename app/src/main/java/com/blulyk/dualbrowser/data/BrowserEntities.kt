package com.blulyk.dualbrowser.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val url: String,
    val title: String,
    val createdAt: Long,
)

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val title: String,
    val visitedAt: Long,
)

@Entity(tableName = "session_tabs")
data class SessionTabEntity(
    @PrimaryKey val id: String,
    val url: String,
    val title: String,
    val position: Int,
)

@Entity(tableName = "session_meta")
data class SessionMetaEntity(
    @PrimaryKey val key: Int = 0,
    val focusedTabId: String?,
    val lowerTabId: String?,
)

data class RestoredSession(
    val tabs: List<SessionTabEntity>,
    val focusedTabId: String?,
    val lowerTabId: String?,
)

