package com.gitlab.j_m_hoffmann.meditate.di

import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module

@Module
abstract class ViewModelModule {

    @Binds
    abstract fun bindViewModelFactory(factory: MeditateViewModelFactory): ViewModelProvider.Factory
}