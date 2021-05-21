package com.gitlab.j_m_hoffmann.meditate

import androidx.work.ExistingPeriodicWorkPolicy.KEEP
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.gitlab.j_m_hoffmann.meditate.di.DaggerApplicationComponent
import com.gitlab.j_m_hoffmann.meditate.worker.StreakResetWorker
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit.HOURS

const val STREAK_RESET_WORKER = "com.gitlab.j_m_hoffmann.meditate.streak_reset_worker"

open class MeditateApplication : DaggerApplication() {

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerApplicationComponent.factory().create(applicationContext)
    }

    override fun onCreate() {
        super.onCreate()
        setupStreakReset()
    }

    private fun setupStreakReset() = CoroutineScope(Dispatchers.Default).launch {

        WorkManager.getInstance(applicationContext)
            .enqueueUniquePeriodicWork(
                STREAK_RESET_WORKER,
                KEEP,
                PeriodicWorkRequestBuilder<StreakResetWorker>(12, HOURS).build()
            )
    }
}