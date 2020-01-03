package com.gitlab.j_m_hoffmann.meditate.ui.progress

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.gitlab.j_m_hoffmann.meditate.MeditateApplication
import com.gitlab.j_m_hoffmann.meditate.R.string
import com.gitlab.j_m_hoffmann.meditate.db.Dao
import kotlinx.coroutines.launch

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

    val streak: LiveData<Int>
        get() = _streak
    private val _streak = MutableLiveData(preferences.getInt(keyStreakValue, 0))

    init {
        viewModelScope.launch {
            _countSessions.value = dao.countSessions()
            _durationAverage.value = dao.durationAverage()
            _durationLongest.value = dao.durationLongest()
            _durationTotal.value = dao.durationTotal()
        }
    }
}
