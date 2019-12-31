package com.gitlab.j_m_hoffmann.meditate.ui.progress

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitlab.j_m_hoffmann.meditate.db.Dao
import kotlinx.coroutines.launch

class ProgressViewModel(private val dao: Dao) : ViewModel() {

    private val _countSessions = MutableLiveData<Int>(0)
    val countSessions: LiveData<Int>
        get() = _countSessions

    private val _durationAverage = MutableLiveData<Long>(0L)
    val durationAverage: LiveData<Long>
        get() = _durationAverage

    private val _durationLongest = MutableLiveData<Long>(0L)
    val durationLongest: LiveData<Long>
        get() = _durationLongest

    private val _durationTotal = MutableLiveData<Long>(0L)
    val durationTotal: LiveData<Long>
        get() = _durationTotal

    init {
        viewModelScope.launch {
            _countSessions.value = dao.countSessions()
            _durationAverage.value = dao.durationAverage()
            _durationLongest.value = dao.durationLongest()
            _durationTotal.value = dao.durationTotal()
        }
    }
}
