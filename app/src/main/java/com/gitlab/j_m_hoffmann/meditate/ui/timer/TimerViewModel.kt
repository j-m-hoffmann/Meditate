package com.gitlab.j_m_hoffmann.meditate.ui.timer

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.gitlab.j_m_hoffmann.meditate.R
import com.gitlab.j_m_hoffmann.meditate.ui.util.second
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

//const val defaultSessionDelay: Long = 15 * second
//const val defaultSessionLength: Long = 15 * minute
// for testing
//const val defaultSessionDelay: Long = 0 * second
const val defaultSessionDelay: Long = 5 * second
const val defaultSessionLength: Long = 5 * second

const val REQUEST_CODE = 0

class TimerViewModel(val app: Application) : AndroidViewModel(app) {

    private val preferences = PreferenceManager.getDefaultSharedPreferences(app)

    //region Variables

    private var delayTimer: CountDownTimer? = null

    private var sessionDelay = preferences.getLong(
        app.getString(R.string.key_session_delay),
        defaultSessionDelay
    )

    private var sessionLength = preferences.getLong(
        app.getString(R.string.key_session_length),
        defaultSessionLength
    )

    private var timer: CountDownTimer? = null

    private var timeSpentMeditating = 0L

    //endregion

    //region LiveData

    private val _delayTimeRemaining = MutableLiveData<Long>()
    val delayTimeRemaining: LiveData<Long>
        get() = _delayTimeRemaining

    private val _delayTimeVisible = MutableLiveData<Boolean>(false)
    val delayTimeVisible: LiveData<Boolean>
        get() = _delayTimeVisible

    private val _sessionInProgress = MutableLiveData<Boolean>(false)
    val sessionInProgress: LiveData<Boolean>
        get() = _sessionInProgress

    private val _sessionPaused = MutableLiveData<Boolean>(false)
    val sessionPaused: LiveData<Boolean>
        get() = _sessionPaused

    private val _timeRemaining = MutableLiveData<Long>()
    val timeRemaining: LiveData<Long>
        get() = _timeRemaining

    //endregion

    init {
        _timeRemaining.value = sessionLength
    }

    //region PublicFunctions

    fun endSession() {
        cancelDelayTimer()
        cancelTimer()
        _timeRemaining.value = sessionLength

        _sessionInProgress.value = false
    }

    fun pauseOrResumeSession() = if (_sessionPaused.value!!) resumeSession() else pauseSession()

    fun startSession() {
        _sessionInProgress.value = true

        startTimer(sessionLength, sessionDelay)
    }
    //endregion

    //region PrivateFunctions

    private fun cancelDelayTimer() {
        delayTimer?.cancel()
        delayTimer = null
        _delayTimeVisible.value = false
    }

    private fun cancelTimer() {
        timer?.cancel()
        timer = null
    }

    private fun pauseSession() {
        _sessionPaused.value = true
        cancelTimer()
        cancelDelayTimer()
    }

    private fun resumeSession() {
        _sessionPaused.value = false
        startTimer(_timeRemaining.value!!, _delayTimeRemaining.value ?: 0)
    }

    private fun saveSession() {
        // save session
        // sendNotification()
    }

    private fun startTimer(duration: Long, delay: Long = 0L) = viewModelScope.launch {
/*
        _timeRemaining.value = duration

        val sessionEnds = SystemClock.elapsedRealtime() + duration + delay

        timer = object : CountDownTimer(sessionEnds, second) {

            override fun onTick(millisUntilFinished: Long) {
                val timeLeft = sessionEnds - SystemClock.elapsedRealtime()
                // interval?
                if (timeLeft >= second) {
                    _timeRemaining.value = timeLeft
                    timeSpentMeditating += second
                } else {
                    onFinish()
                }
            }
*/

        timer = object : CountDownTimer(duration, second) {

            override fun onTick(millisUntilFinished: Long) {
                if (millisUntilFinished >= second) {
                    _timeRemaining.value = millisUntilFinished
                    timeSpentMeditating += second
                } else {
                    onFinish()
                }
            }

            override fun onFinish() {
                saveSession()
                endSession()
            }

        }

        if (delay > 0L) {
            _delayTimeRemaining.value = delay
            _delayTimeVisible.value = true

//            val delayEnds = SystemClock.elapsedRealtime() + delay
//            delayTimer = object : CountDownTimer(delayEnds, second) {
            delayTimer = object : CountDownTimer(delay, second) {

                override fun onTick(millisUntilFinished: Long) {
//                    val remainingDelayTime = delayEnds - SystemClock.elapsedRealtime()
//                    if (remainingDelayTime >= second) {
//                        _delayTimeRemaining.value = remainingDelayTime
                    if (millisUntilFinished >= second) {
                        _delayTimeRemaining.value = millisUntilFinished
                    } else {
                        onFinish()
                    }
                }

                override fun onFinish() {
                    _delayTimeRemaining.value = 0
                    cancelDelayTimer()
                }
            }

            delayTimer?.start()

            delay(delay)
        }

        timer?.start()
    }
    //endregion
}
