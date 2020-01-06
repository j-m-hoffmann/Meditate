package com.gitlab.j_m_hoffmann.meditate

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy.KEEP
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.gitlab.j_m_hoffmann.meditate.db.Db
import com.gitlab.j_m_hoffmann.meditate.db.getDatabase
import com.gitlab.j_m_hoffmann.meditate.ui.util.STREAK_RESET_WORKER
import com.gitlab.j_m_hoffmann.meditate.worker.StreakResetWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit.DAYS

class MeditateApplication : Application() {

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    val database: Db
        get() = getDatabase(this)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            setupStreakReset()
        }
    }

    private fun setupStreakReset() {

        val periodicWork = PeriodicWorkRequestBuilder<StreakResetWorker>(1, DAYS).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            STREAK_RESET_WORKER,
            KEEP,
            periodicWork
        )
    }
}