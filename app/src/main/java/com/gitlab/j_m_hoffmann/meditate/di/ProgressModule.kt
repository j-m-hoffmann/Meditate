package com.gitlab.j_m_hoffmann.meditate.di

import androidx.lifecycle.ViewModel
import com.gitlab.j_m_hoffmann.meditate.ui.progress.ProgressFragment
import com.gitlab.j_m_hoffmann.meditate.ui.progress.ProgressViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
abstract class ProgressModule {

    @ContributesAndroidInjector(modules = [ViewModelModule::class])
    abstract fun progressFragment(): ProgressFragment

    @Binds
    @IntoMap
    @ViewModelKey(ProgressViewModel::class)
    abstract fun bindViewModel(viewModel: ProgressViewModel): ViewModel
}