package com.gitlab.j_m_hoffmann.meditate.di

import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [DbModule::class])
interface ApplicationComponent