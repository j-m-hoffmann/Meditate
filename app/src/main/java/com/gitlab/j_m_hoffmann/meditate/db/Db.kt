package com.gitlab.j_m_hoffmann.meditate.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Session::class], version = 1, exportSchema = false)
abstract class Db : RoomDatabase() {

    abstract val dao: Dao
}
