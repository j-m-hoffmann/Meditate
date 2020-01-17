package com.gitlab.j_m_hoffmann.meditate.di

import androidx.lifecycle.ViewModel
import com.gitlab.j_m_hoffmann.meditate.ui.session.SessionFragment
import com.gitlab.j_m_hoffmann.meditate.ui.session.SessionViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
abstract class SessionModule {

    @ContributesAndroidInjector(modules = [ViewModelModule::class])
    abstract fun sessionFragment(): SessionFragment

    @Binds
    @IntoMap
    @ViewModelKey(SessionViewModel::class)
    abstract fun bindViewModel(viewModel: SessionViewModel): ViewModel
}
