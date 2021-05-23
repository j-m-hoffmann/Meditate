package com.gitlab.j_m_hoffmann.meditate.worker

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.gitlab.j_m_hoffmann.meditate.R.string.key_streak_expires
import com.gitlab.j_m_hoffmann.meditate.R.string.key_streak_value
import com.gitlab.j_m_hoffmann.meditate.extensions.updateWidget
import com.gitlab.j_m_hoffmann.meditate.widget.StreakWidget
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId

class StreakResetWorker(val context: Context, parameters: WorkerParameters) : Worker(context, parameters) {

    override fun doWork(): Result {

        val preferences = PreferenceManager.getDefaultSharedPreferences(context)

        val zoneId = ZoneId.systemDefault()
        val zoneOffset = OffsetDateTime.now(zoneId).offset
        val expiryEpochSecond = preferences.getLong(context.getString(key_streak_expires), Long.MAX_VALUE)
        val streakExpiryDate = LocalDateTime.ofEpochSecond(expiryEpochSecond, 0, zoneOffset)

        if (LocalDateTime.now(zoneId).isAfter(streakExpiryDate)) {
            preferences.edit(commit = true) { putInt(context.getString(key_streak_value), 0) }

            context.updateWidget<StreakWidget>()
        }

        return Result.success()
    }
}