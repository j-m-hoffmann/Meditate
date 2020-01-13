package com.gitlab.j_m_hoffmann.meditate.ui.progress

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.gitlab.j_m_hoffmann.meditate.MeditateApplication
import com.gitlab.j_m_hoffmann.meditate.R
import com.gitlab.j_m_hoffmann.meditate.R.string
import com.gitlab.j_m_hoffmann.meditate.db.Dao
import com.gitlab.j_m_hoffmann.meditate.extensions.locale
import com.gitlab.j_m_hoffmann.meditate.extensions.toPlural
import kotlinx.coroutines.launch
import java.text.DateFormat

class ProgressViewModel(private val app: MeditateApplication, private val dao: Dao) : ViewModel() {

    private val keyStreakValue = app.getString(string.key_streak_value)

    private val preferences = PreferenceManager.getDefaultSharedPreferences(app)

    val countSessions: LiveData<Int>
        get() = _countSessions
    private val _countSessions = MutableLiveData(0)

    val durationAverage: LiveData<Long>
        get() = _durationAverage
    private val _durationAverage = MutableLiveData(0L)

    val durationLongest: LiveData<Long>
        get() = _durationLongest
    private val _durationLongest = MutableLiveData(0L)

    val durationTotal: LiveData<Long>
        get() = _durationTotal
    private val _durationTotal = MutableLiveData(0L)

    private val _lastSessionDate = MutableLiveData(0L)

    val lastSessionDate = Transformations.map(_lastSessionDate) { date ->
        when (date) {
            0L -> ""
            else -> DateFormat.getDateInstance(DateFormat.FULL, app.locale()).format(date)
        }
    }

    private val _streak = MutableLiveData(preferences.getInt(keyStreakValue, 0))

    val streak = Transformations.map(_streak) { it.toPlural(R.plurals.days, R.string.empty, app) }

    init {
        viewModelScope.launch {
            _countSessions.value = dao.countSessions()
            _durationAverage.value = dao.durationAverage()
            _durationLongest.value = dao.durationLongest()
            _durationTotal.value = dao.durationTotal()
            _lastSessionDate.value = dao.lastSessionDate() ?: 0L
        }
    }
}
