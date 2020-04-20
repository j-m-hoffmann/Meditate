package com.gitlab.j_m_hoffmann.meditate.di

import com.gitlab.j_m_hoffmann.meditate.ui.progress.ProgressFragment
import com.gitlab.j_m_hoffmann.meditate.ui.session.SessionFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentModule {

    @ContributesAndroidInjector(modules = [ViewModelModule::class])
    abstract fun progressFragment(): ProgressFragment

    @ContributesAndroidInjector(modules = [ViewModelModule::class])
    abstract fun sessionFragment(): SessionFragment
}