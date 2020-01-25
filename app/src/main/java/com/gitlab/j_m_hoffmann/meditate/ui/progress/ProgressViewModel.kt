package com.gitlab.j_m_hoffmann.meditate.ui.progress

import android.content.Context
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.preference.PreferenceManager
import com.gitlab.j_m_hoffmann.meditate.R
import com.gitlab.j_m_hoffmann.meditate.R.string.key_streak_value
import com.gitlab.j_m_hoffmann.meditate.extensions.locale
import com.gitlab.j_m_hoffmann.meditate.extensions.toPlural
import com.gitlab.j_m_hoffmann.meditate.repository.SessionRepository
import java.text.DateFormat
import javax.inject.Inject

class ProgressViewModel @Inject constructor(app: Context, repository: SessionRepository) : ViewModel() {

    private val preferences = PreferenceManager.getDefaultSharedPreferences(app)

    val countSessions = liveData { emit(repository.countSessions()) }

    val durationAverage = liveData { emit(repository.durationAverage()) }

    val durationLongest = liveData { emit(repository.durationLongest()) }

    val durationTotal = liveData { emit(repository.durationTotal()) }

    private val _lastSessionDate = liveData { emit(repository.lastSessionDate()) }

    val lastSessionDate = Transformations.map(_lastSessionDate) { date ->
        when (date) {
            0L -> ""
            else -> DateFormat.getDateInstance(DateFormat.FULL, app.locale()).format(date)
        }
    }

    private val _longestStreak = liveData { emit(preferences.getInt(app.getString(key_streak_value), 0)) }

    val longestStreak = Transformations.map(_longestStreak) { it.toPlural(R.plurals.days, R.string.empty, app) }
}
