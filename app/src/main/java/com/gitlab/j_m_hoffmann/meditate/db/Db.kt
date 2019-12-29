package com.gitlab.j_m_hoffmann.meditate.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Session::class], version = 1, exportSchema = false)
abstract class Db : RoomDatabase() {

    abstract val dao: Dao
}

@Volatile
private lateinit var INSTANCE: Db

fun getDatabase(context: Context) = synchronized(Db::class.java) {
    if (!::INSTANCE.isInitialized) {
        INSTANCE = Room.databaseBuilder(
            context.applicationContext,
            Db::class.java,
            "sessions"
        )
            .build()
    }
    INSTANCE
}
