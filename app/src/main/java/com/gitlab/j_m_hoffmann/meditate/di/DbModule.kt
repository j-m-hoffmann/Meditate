package com.gitlab.j_m_hoffmann.meditate.di

import android.content.Context
import androidx.room.Room
import com.gitlab.j_m_hoffmann.meditate.repository.db.Dao
import com.gitlab.j_m_hoffmann.meditate.repository.db.Db
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object DbModule {

    @Provides
    fun provideDatabase(@ApplicationContext context: Context): Db = Room.databaseBuilder(
        context,
        Db::class.java,
        "sessions"
    ).build()

    @Provides
    fun provideDao(db: Db): Dao = db.dao
}
