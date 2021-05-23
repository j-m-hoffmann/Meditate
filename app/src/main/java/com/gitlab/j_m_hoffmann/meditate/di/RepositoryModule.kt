package com.gitlab.j_m_hoffmann.meditate.di

import com.gitlab.j_m_hoffmann.meditate.repository.DefaultRepository
import com.gitlab.j_m_hoffmann.meditate.repository.SessionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
abstract class RepositoryModule {

    @Binds
    abstract fun repository(repository: DefaultRepository): SessionRepository
}
