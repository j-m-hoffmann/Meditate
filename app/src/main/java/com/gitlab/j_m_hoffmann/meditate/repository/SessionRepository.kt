package com.gitlab.j_m_hoffmann.meditate.repository

import com.gitlab.j_m_hoffmann.meditate.repository.db.Session

interface SessionRepository {
    suspend fun insert(session: Session)

    suspend fun countSessions(): Int

    suspend fun durationAverage(): Long

    suspend fun durationLongest(): Long

    suspend fun durationTotal(): Long

    suspend fun lastSessionDate(): Long

    fun updateStreak(days: Int, expiryEpochSecond: Long)

    val currentStreak: Int

    val doNotDisturb: Boolean

    var longestStreak: Int

    val sessionDelay: Long

    var sessionLength: Long
}

