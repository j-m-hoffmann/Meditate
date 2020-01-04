package com.gitlab.j_m_hoffmann.meditate.worker

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.gitlab.j_m_hoffmann.meditate.R.string.key_streak_expires
import com.gitlab.j_m_hoffmann.meditate.R.string.key_streak_value
import com.gitlab.j_m_hoffmann.meditate.util.updateWidget

class StreakResetWorker(val context: Context, parameters: WorkerParameters) : Worker(context, parameters) {

    override fun doWork(): Result {

        val preferences = PreferenceManager.getDefaultSharedPreferences(context)

        val streakExpires = preferences.getLong(context.getString(key_streak_expires), Long.MAX_VALUE)

        if (streakExpires < System.currentTimeMillis()) {
            preferences.edit(commit = true) { putInt(context.getString(key_streak_value), 0) }

            updateWidget((context as Application))
        }

        return Result.success()
    }
}