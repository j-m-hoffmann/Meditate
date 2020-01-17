package com.gitlab.j_m_hoffmann.meditate.di

import com.gitlab.j_m_hoffmann.meditate.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MainActivityModule {

    @ContributesAndroidInjector(modules = [ViewModelModule::class])
    abstract fun mainActivity(): MainActivity
}
