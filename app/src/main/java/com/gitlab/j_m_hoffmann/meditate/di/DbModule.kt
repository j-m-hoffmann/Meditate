package com.gitlab.j_m_hoffmann.meditate.di

import android.content.Context
import androidx.room.Room
import com.gitlab.j_m_hoffmann.meditate.repository.db.Dao
import com.gitlab.j_m_hoffmann.meditate.repository.db.Db
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object DbModule {

    @Singleton
    @Provides
    fun provideDatabase(context: Context): Db {
        return Room.databaseBuilder(
            context,
            Db::class.java,
            "sessions"
        )
            .build()
    }

    @Singleton
    @Provides
    fun provideDao(db: Db): Dao = db.dao
}
