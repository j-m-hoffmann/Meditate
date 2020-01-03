package com.gitlab.j_m_hoffmann.meditate.ui.timer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.os.SystemClock
import androidx.core.app.AlarmManagerCompat
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
import com.gitlab.j_m_hoffmann.meditate.receiver.SessionEndedReceiver
import com.gitlab.j_m_hoffmann.meditate.ui.util.MINUTE
import com.gitlab.j_m_hoffmann.meditate.ui.util.REQUEST_CODE
import com.gitlab.j_m_hoffmann.meditate.ui.util.SECOND
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val DEFAULT_SESSION_DELAY: Long = 15 * SECOND
const val DEFAULT_SESSION_LENGTH: Long = 15 * MINUTE
const val FIVE_MINUTES: Long = 5 * MINUTE
const val MIN_SESSION_LENGTH: Long = 10 * MINUTE

class TimerViewModel(val app: MeditateApplication, private val dao: Dao) : ViewModel() {

    private val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private val keySessionLength = app.getString(key_session_length)

    private val notificationIntent = Intent(app, SessionEndedReceiver::class.java)

    private val notificationPendingIntent = PendingIntent.getBroadcast(
        app,
        REQUEST_CODE,
        notificationIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    private val preferences = PreferenceManager.getDefaultSharedPreferences(app)

    //region Variables

    private var delayTimer: CountDownTimer? = null

    private var sessionDelay = preferences.getLong(app.getString(key_session_delay), DEFAULT_SESSION_DELAY)

    private var sessionLength = preferences.getLong(keySessionLength, DEFAULT_SESSION_LENGTH)

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

    /*
    init {
        // used for testing
        val session1 = Session(System.currentTimeMillis(), 10 * MINUTE)
        viewModelScope.launch {
            dao.insertSeveral(
                session1
                , Session(session1.date - DAY, FIVE_MINUTES)
                , Session(session1.date - 2 * DAY, 15 * MINUTE)
                , Session(session1.date - 3 * DAY, 3 * MINUTE)
                , Session(session1.date - 4 * DAY, 20 * MINUTE)
                , Session(session1.date - 5 * DAY, 20 * SECOND)
                , Session(session1.date - 6 * DAY, 1 * HOUR)
                , Session(session1.date - 7 * DAY, 10000 * HOUR)
                , Session(session1.date - 8 * DAY, 1000000 * HOUR)
            )
        }
    }
    */

    //region PublicFunctions

    fun decrementDuration() {
        sessionLength -= FIVE_MINUTES

        if (sessionLength <= MIN_SESSION_LENGTH) {
            sessionLength = MIN_SESSION_LENGTH
            _decrementEnabled.value = false
        }

        _timeRemaining.value = sessionLength
    }

    fun endSession() {
        cancelAlarm()
        cancelDelayTimer()
        cancelTimer()

        if (_delayTimeRemaining.value == 0L) {
            showDiscardAndSaveButtons()
        } else {
            resetSession()
        }
    }

    fun incrementDuration() {
        sessionLength += FIVE_MINUTES
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
    private fun cancelAlarm() = alarmManager.cancel(notificationPendingIntent)

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
        cancelAlarm()
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

        timer = object : CountDownTimer(duration, SECOND) {

            override fun onTick(millisUntilFinished: Long) {
                if (millisUntilFinished >= SECOND) {
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
            delayTimer = object : CountDownTimer(delay, SECOND) {

                override fun onTick(millisUntilFinished: Long) {
//                    val remainingDelayTime = delayEnds - SystemClock.elapsedRealtime()
//                    if (remainingDelayTime >= second) {
//                        _delayTimeRemaining.value = remainingDelayTime
                    if (millisUntilFinished >= SECOND) {
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

        AlarmManagerCompat.setExactAndAllowWhileIdle(
            alarmManager,
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + duration,
            notificationPendingIntent
        )
    }
    //endregion
}
