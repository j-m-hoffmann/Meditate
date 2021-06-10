package com.gitlab.j_m_hoffmann.meditate.ui.progress

import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.gitlab.j_m_hoffmann.meditate.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle.MEDIUM
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(repository: SessionRepository) : ViewModel() {

    val countSessions = liveData { emit(repository.countSessions()) }

    val durationAverage = liveData { emit(repository.durationAverage()) }

    val durationLongest = liveData { emit(repository.durationLongest()) }

    val durationTotal = liveData { emit(repository.durationTotal()) }

    private val _lastSessionDate = liveData { emit(repository.lastSessionDate()) }

    val lastSessionDate = Transformations.map(_lastSessionDate) { date ->
        when (date) {
            0L -> ""
            else -> {
                val zoneOffset = OffsetDateTime.now(ZoneId.systemDefault()).offset

                LocalDateTime
                    .ofEpochSecond(date, 0, zoneOffset)
                    .toLocalDate()
                    .format(DateTimeFormatter.ofLocalizedDate(MEDIUM))
            }
        }
    }

    val longestStreak = liveData { emit(repository.longestStreak) }
}
