package com.gitlab.j_m_hoffmann.meditate.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gitlab.j_m_hoffmann.meditate.ui.progress.ProgressViewModel
import com.gitlab.j_m_hoffmann.meditate.ui.session.SessionViewModel
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.PROPERTY_GETTER
import kotlin.annotation.AnnotationTarget.PROPERTY_SETTER
import kotlin.reflect.KClass

@Target(FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER)
@Retention(RUNTIME)
@MapKey
annotation class ViewModelKey(val value: KClass<out ViewModel>)

@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(ProgressViewModel::class)
    abstract fun progressViewModel(viewModel: ProgressViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SessionViewModel::class)
    abstract fun sessionViewModel(viewModel: SessionViewModel): ViewModel

    @Binds
    abstract fun viewModelFactory(factory: MeditateViewModelFactory): ViewModelProvider.Factory
}
