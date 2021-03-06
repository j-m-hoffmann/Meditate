package com.gitlab.j_m_hoffmann.meditate.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.gitlab.j_m_hoffmann.meditate.repository.db.Db
import com.gitlab.j_m_hoffmann.meditate.repository.db.Session
import com.gitlab.j_m_hoffmann.meditate.util.DAY
import com.gitlab.j_m_hoffmann.meditate.util.MINUTE
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.*

@RunWith(AndroidJUnit4::class)
class DefaultRepositoryTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: Db
    private lateinit var repository: DefaultRepository

    private val now = System.currentTimeMillis()

    private val sessions = listOf(
        Session(now, 5 * MINUTE),
        Session(now - 1 * DAY, 10 * MINUTE),
        Session(now - 2 * DAY, 15 * MINUTE)
    )

    private suspend fun insertSessions() = sessions.forEach { repository.insert(it) }

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, Db::class.java).build()
        repository = DefaultRepository(database.dao)
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun countSessionsReturnsZeroWhenRepositoryIsEmpty() = runBlocking {
        val countSessions = repository.countSessions()

        assertThat(countSessions).isEqualTo(0)
    }

    @Test
    fun countSessionsReturnsTheCorrectNumberOfSessions() = runBlocking {
        insertSessions()

        val countSessions = repository.countSessions()

        assertThat(countSessions).isEqualTo(3)
    }

    @Test
    fun durationAverageReturnsZeroWhenRepositoryIsEmpty() = runBlocking {
        val durationAverage = repository.durationAverage()

        assertThat(durationAverage).isEqualTo(0)
    }

    @Test
    fun durationAverageReturnsTheCorrectAverage() = runBlocking {
        insertSessions()

        val durationAverage = repository.durationAverage()

        assertThat(durationAverage).isEqualTo(10 * MINUTE)
    }

    @Test
    fun durationLongestReturnsZeroWhenRepositoryIsEmpty() = runBlocking {
        val durationLongest = repository.durationLongest()

        assertThat(durationLongest).isEqualTo(0)
    }

    @Test
    fun durationLongestReturnsTheLongestSession() = runBlocking {
        insertSessions()

        val durationLongest = repository.durationLongest()

        assertThat(durationLongest).isEqualTo(15 * MINUTE)
    }

    @Test
    fun durationTotalReturnsZeroWhenRepositoryIsEmpty() = runBlocking {
        val durationTotal = repository.durationTotal()

        assertThat(durationTotal).isEqualTo(0)
    }

    @Test
    fun durationTotalReturnsTheSumOfAllSessions() = runBlocking {
        insertSessions()

        val durationTotal = repository.durationTotal()

        assertThat(durationTotal).isEqualTo(30 * MINUTE)
    }

    @Test
    fun lastSessionDateReturnsZeroWhenRepositoryIsEmpty() = runBlocking {
        val lastSessionDate = repository.lastSessionDate()

        assertThat(lastSessionDate).isEqualTo(0)
    }

    @Test
    fun lastSessionDateReturnsTheLatestSession() = runBlocking {
        insertSessions()

        val lastSessionDate = repository.lastSessionDate()

        assertThat(lastSessionDate).isEqualTo(now)
    }
}