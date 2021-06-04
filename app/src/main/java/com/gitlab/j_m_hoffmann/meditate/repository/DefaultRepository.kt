package com.gitlab.j_m_hoffmann.meditate.repository

import com.gitlab.j_m_hoffmann.meditate.repository.db.Dao
import com.gitlab.j_m_hoffmann.meditate.repository.db.Session
import javax.inject.Inject

class DefaultRepository @Inject constructor(private val dao: Dao) : SessionRepository {

    override suspend fun insert(session: Session) = dao.insert(session)

    override suspend fun countSessions(): Int = dao.countSessions()

    override suspend fun durationAverage(): Long = dao.durationAverage() ?: 0

    override suspend fun durationLongest(): Long = dao.durationLongest() ?: 0

    override suspend fun durationTotal(): Long = dao.durationTotal() ?: 0

    override suspend fun lastSessionDate(): Long = dao.lastSessionDate() ?: 0
}
