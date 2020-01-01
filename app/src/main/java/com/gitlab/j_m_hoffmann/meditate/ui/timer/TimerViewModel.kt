package com.gitlab.j_m_hoffmann.meditate.ui.timer

import android.os.CountDownTimer
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.gitlab.j_m_hoffmann.meditate.MeditateApplication
import com.gitlab.j_m_hoffmann.meditate.R.string.key_session_delay
import com.gitlab.j_m_hoffmann.meditate.R.string.key_session_length
import com.gitlab.j_m_hoffmann.meditate.db.Dao
import com.gitlab.j_m_hoffmann.meditate.db.Session
import com.gitlab.j_m_hoffmann.meditate.ui.util.minute
import com.gitlab.j_m_hoffmann.meditate.ui.util.second
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val defaultSessionDelay: Long = 15 * second
const val defaultSessionLength: Long = 15 * minute
const val fiveMinutes: Long = 5 * minute
const val minSessionLength: Long = 10 * minute
// for testing
//const val defaultSessionDelay: Long = 0 * second
//const val defaultSessionDelay: Long = 5 * second
//const val defaultSessionLength: Long = 5 * second

class TimerViewModel(val app: MeditateApplication, private val dao: Dao) : ViewModel() {

    private val keySessionLength = app.getString(key_session_length)
    private val preferences = PreferenceManager.getDefaultSharedPreferences(app)

    //region Variables

    private var delayTimer: CountDownTimer? = null

    private var sessionDelay = preferences.getLong(
        app.getString(key_session_delay),
        defaultSessionDelay
    )

    private var sessionLength = preferences.getLong(
        keySessionLength,
        defaultSessionLength
    )

    private var timer: CountDownTimer? = null

    //endregion

    //region LiveData

    val decrementEnabled: LiveData<Boolean>
        get() = _decrementEnabled
    private val _decrementEnabled = MutableLiveData(true)

    val delayTimeRemaining: LiveData<Long>
        get() = _delayTimeRemaining
    private val _delayTimeRemaining = MutableLiveData(sessionDelay)

    val delayTimeVisible: LiveData<Boolean>
        get() = _delayTimeVisible
    private val _delayTimeVisible = MutableLiveData(false)

    val sessionInProgress: LiveData<Boolean>
        get() = _sessionInProgress
    private val _sessionInProgress = MutableLiveData(false)

    val sessionPaused: LiveData<Boolean>
        get() = _sessionPaused
    private val _sessionPaused = MutableLiveData(false)

    val showDiscardAndSave: LiveData<Boolean>
        get() = _showDiscardAndSave
    private val _showDiscardAndSave = MutableLiveData(false)

    val showEndAndPause: LiveData<Boolean>
        get() = _showEndAndPause
    private val _showEndAndPause = MutableLiveData(false)

    val showStart: LiveData<Boolean>
        get() = _showStart
    private val _showStart = MutableLiveData(true)

    val timeRemaining: LiveData<Long>
        get() = _timeRemaining
    private val _timeRemaining = MutableLiveData(sessionLength)

    //endregion

    init {
/*
        // used for testing
        val session1 = Session(System.currentTimeMillis(), tenMinutes)
        viewModelScope.launch {
            dao.insertSeveral(
                session1
                , Session(session1.date - day, fiveMinutes)
                , Session(session1.date - 2 * day, 15 * minute)
                , Session(session1.date - 3 * day, 3 * minute)
                , Session(session1.date - 4 * day, 20 * minute)
                , Session(session1.date - 5 * day, 20 * second)
                , Session(session1.date - 6 * day, 1 * hour)
                , Session(session1.date - 7 * day, 10000 * hour)
                , Session(session1.date - 8 * day, 1000000 * hour)
            )
        }
*/
    }

    //region PublicFunctions

    fun decrementDuration() {
        sessionLength -= fiveMinutes

        if (sessionLength <= minSessionLength) {
            sessionLength = minSessionLength
            _decrementEnabled.value = false
        }

        _timeRemaining.value = sessionLength
    }

    fun endSession() {
        cancelDelayTimer()
        cancelTimer()

        if (_delayTimeRemaining.value == 0L) {
            showDiscardAndSaveButtons()
        } else {
            resetSession()
        }
    }

    fun incrementDuration() {
        sessionLength += fiveMinutes
        _decrementEnabled.value = true
        _timeRemaining.value = sessionLength
    }

    fun pauseOrResumeSession() = if (_sessionPaused.value!!) resumeSession() else pauseSession()

    fun resetSession() {
        _timeRemaining.value = sessionLength
        _sessionInProgress.value = false
        showStartButton()
    }

    fun saveSession() {
        persistSession(sessionLength - timeRemaining.value!!)
        resetSession()
    }

    fun startSession() {
        _sessionInProgress.value = true

        preferences.edit { putLong(keySessionLength, sessionLength) }

        // TODO show Message

        showEndAndPauseButtons()

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
        startTimer(_timeRemaining.value!!, _delayTimeRemaining.value!!)
    }

    private fun persistSession(length: Long) = viewModelScope.launch {
        dao.insert(Session(System.currentTimeMillis(), length))
    }

    private fun showDiscardAndSaveButtons() {
        _showEndAndPause.value = false
        _showStart.value = false

        _showDiscardAndSave.value = true
    }

    private fun showEndAndPauseButtons() {
        _showDiscardAndSave.value = false
        _showStart.value = false

        _showEndAndPause.value = true
    }

    private fun showStartButton() {
        _showDiscardAndSave.value = false
        _showEndAndPause.value = false

        _showStart.value = true
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
                } else {
                    onFinish()
                }
            }

            override fun onFinish() {
                cancelTimer()
                persistSession(sessionLength)
                resetSession()
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
