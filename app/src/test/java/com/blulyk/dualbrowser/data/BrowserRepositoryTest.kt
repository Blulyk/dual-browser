package com.blulyk.dualbrowser.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.blulyk.dualbrowser.domain.BrowserTab
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class BrowserRepositoryTest {
    private lateinit var database: BrowserDatabase
    private lateinit var repository: BrowserRepository
    private var now = 100L

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, BrowserDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = BrowserRepository(database.browserDao(), clock = { now++ })
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun privateVisitsAreNotPersisted() = runTest {
        repository.recordVisit(BrowserTab("private", "https://private.example", isPrivate = true))

        assertTrue(repository.history.first().isEmpty())
    }

    @Test
    fun latestNormalVisitAppearsFirst() = runTest {
        repository.recordVisit(BrowserTab("one", "https://one.example", title = "One"))
        repository.recordVisit(BrowserTab("two", "https://two.example", title = "Two"))

        assertEquals("https://two.example", repository.history.first().first().url)
    }

    @Test
    fun savedSessionExcludesPrivateTabs() = runTest {
        val normal = BrowserTab("normal", "https://example.com")
        val private = BrowserTab("private", "https://private.example", isPrivate = true)

        repository.saveSession(listOf(normal, private), focusedTabId = normal.id, lowerTabId = private.id)
        val restored = repository.restoreSession()

        assertEquals(listOf(normal.id), restored.tabs.map(SessionTabEntity::id))
        assertEquals(normal.id, restored.focusedTabId)
        assertEquals(null, restored.lowerTabId)
    }
}

