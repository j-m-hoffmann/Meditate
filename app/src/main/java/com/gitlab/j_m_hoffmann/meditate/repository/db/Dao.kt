package com.gitlab.j_m_hoffmann.meditate.repository.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface Dao {

/*
    // used for testing
    @Insert
    suspend fun insertSeveral(vararg sessions: Session)
*/

    @Insert
    suspend fun insert(session: Session)

    @Query("SELECT COUNT(duration) FROM session")
    suspend fun countSessions(): Int

    @Query("SELECT AVG(duration) FROM session")
    suspend fun durationAverage(): Long

    @Query("SELECT MAX(duration) FROM session")
    suspend fun durationLongest(): Long

    @Query("SELECT SUM(duration) FROM session")
    suspend fun durationTotal(): Long

    @Query("SELECT MAX(date) FROM session")
    suspend fun lastSessionDate(): Long?
}

