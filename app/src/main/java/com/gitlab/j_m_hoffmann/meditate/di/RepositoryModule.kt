package com.gitlab.j_m_hoffmann.meditate.di

import com.gitlab.j_m_hoffmann.meditate.repository.DefaultRepository
import com.gitlab.j_m_hoffmann.meditate.repository.SessionRepository
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module(includes = [DbModule::class])
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun repository(repository: DefaultRepository): SessionRepository
}
