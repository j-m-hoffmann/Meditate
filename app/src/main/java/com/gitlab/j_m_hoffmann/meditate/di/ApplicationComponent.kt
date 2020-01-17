package com.gitlab.j_m_hoffmann.meditate.di

import android.content.Context
import com.gitlab.j_m_hoffmann.meditate.MeditateApplication
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidInjectionModule::class,
        MainActivityModule::class,
        ProgressModule::class,
        RepositoryModule::class,
        SessionModule::class,
        ViewModelModule::class
    ]
)
interface ApplicationComponent : AndroidInjector<MeditateApplication> {

    @Component.Factory
    interface Factory {

        fun create(@BindsInstance applicationContext: Context): ApplicationComponent
    }
}