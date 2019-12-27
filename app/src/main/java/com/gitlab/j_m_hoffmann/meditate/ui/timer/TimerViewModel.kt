package com.gitlab.j_m_hoffmann.meditate.ui.timer

import android.app.Application
import android.os.CountDownTimer
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.gitlab.j_m_hoffmann.meditate.R
import com.gitlab.j_m_hoffmann.meditate.ui.util.second
import kotlinx.coroutines.launch

//const val defaultSessionDelay: Long = 15 * second
//const val defaultSessionLength: Long = 15 * minute
// for testing
const val defaultSessionDelay: Long = 5 * second
const val defaultSessionLength: Long = 5 * second

const val REQUEST_CODE = 0

class TimerViewModel(val app: Application) : AndroidViewModel(app) {

    private val preferences = PreferenceManager.getDefaultSharedPreferences(app)

    private var sessionLength = preferences.getLong(
        app.getString(R.string.key_session_length),
        defaultSessionLength
    )

    private var sessionDelay = preferences.getLong(
        app.getString(R.string.key_session_delay),
        defaultSessionDelay
    )

    private lateinit var timer: CountDownTimer

    private var timeSpentMeditating = 0L

    private val _sessionInProgress = MutableLiveData<Boolean>(false)
    val sessionInProgress: LiveData<Boolean>
        get() = _sessionInProgress

    private val _sessionPaused = MutableLiveData<Boolean>(false)
    val sessionPaused: LiveData<Boolean>
        get() = _sessionPaused

    private val _hideNavigation = MutableLiveData<Boolean>(false)
    val hideNavigation: LiveData<Boolean>
        get() = _hideNavigation

    private val _timeElapsed = MutableLiveData<Long>()
    val timeElapsed: LiveData<Long>
        get() = _timeElapsed

    private val _timeRemaining = MutableLiveData<Long>()
    val timeRemaining: LiveData<Long>
        get() = _timeRemaining

    init {
        _timeRemaining.value = sessionLength
    }

    private fun startTimer(duration: Long) = viewModelScope.launch {

        val sessionEnds = SystemClock.elapsedRealtime() + duration

        timer = object : CountDownTimer(sessionEnds, second) {
            override fun onFinish() {
                // saveSession()
                _sessionInProgress.value = false
                resetTimer()
                // sendNotification()
            }

            override fun onTick(millisUntilFinished: Long) {
                timeSpentMeditating += second
                _timeRemaining.value = sessionEnds - SystemClock.elapsedRealtime()

                // interval?
                if (_timeRemaining.value!! <= 0) {
                    onFinish()
                }
            }
        }

        timer.start()
    }

    fun startSession() {
        _sessionInProgress.value = true

        val sessionPlusDelay = sessionLength + sessionDelay

        _timeRemaining.value = sessionPlusDelay

        startTimer(sessionPlusDelay)
    }

    fun pauseOrResumeSession() = if (_sessionPaused.value!!) resumeSession() else pauseSession()

    private fun pauseSession() {
        _sessionPaused.value = true
        timer.cancel()
    }

    private fun resumeSession() {
        _sessionPaused.value = false
        startTimer(_timeRemaining.value!!)
    }

    fun endSession() {
        _sessionInProgress.value = false

        timer.cancel()
        resetTimer()
    }

    private fun resetTimer() {
        _timeRemaining.value = sessionLength
    }

}
