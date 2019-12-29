package com.gitlab.j_m_hoffmann.meditate.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface Dao {

    @Insert
    suspend fun insert(session: Session)

    @Query("SELECT COUNT(duration) FROM session")
    suspend fun countSessions(): Int

    @Query("SELECT duration FROM session ORDER BY duration DESC LIMIT 1")
    suspend fun longestSession(): Long

    @Query("SELECT AVG(duration) FROM session")
    suspend fun averageDuration(): Long

    @Query("SELECT SUM(duration) FROM session")
    suspend fun totalDuration(): Long
}

