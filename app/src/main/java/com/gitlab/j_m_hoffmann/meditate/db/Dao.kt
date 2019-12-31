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

    @Query("SELECT AVG(duration) FROM session")
    suspend fun durationAverage(): Long

    @Query("SELECT duration FROM session ORDER BY duration DESC LIMIT 1")
    suspend fun durationLongest(): Long

    @Query("SELECT SUM(duration) FROM session")
    suspend fun durationTotal(): Long
}

