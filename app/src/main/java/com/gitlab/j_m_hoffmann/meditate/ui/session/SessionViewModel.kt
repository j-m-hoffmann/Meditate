package com.gitlab.j_m_hoffmann.meditate.ui.session

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.os.SystemClock
import androidx.core.app.AlarmManagerCompat
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.preference.PreferenceManager
import com.gitlab.j_m_hoffmann.meditate.R
import com.gitlab.j_m_hoffmann.meditate.R.string.default_delay
import com.gitlab.j_m_hoffmann.meditate.R.string.key_last_session
import com.gitlab.j_m_hoffmann.meditate.R.string.key_session_delay
import com.gitlab.j_m_hoffmann.meditate.R.string.key_session_length
import com.gitlab.j_m_hoffmann.meditate.R.string.key_streak_expires
import com.gitlab.j_m_hoffmann.meditate.R.string.key_streak_longest
import com.gitlab.j_m_hoffmann.meditate.R.string.key_streak_value
import com.gitlab.j_m_hoffmann.meditate.extensions.toPlural
import com.gitlab.j_m_hoffmann.meditate.extensions.updateWidget
import com.gitlab.j_m_hoffmann.meditate.receiver.SessionEndedReceiver
import com.gitlab.j_m_hoffmann.meditate.repository.SessionRepository
import com.gitlab.j_m_hoffmann.meditate.repository.db.Session
import com.gitlab.j_m_hoffmann.meditate.ui.session.State.Aborted
import com.gitlab.j_m_hoffmann.meditate.ui.session.State.Ended
import com.gitlab.j_m_hoffmann.meditate.ui.session.State.InProgress
import com.gitlab.j_m_hoffmann.meditate.util.MINUTE
import com.gitlab.j_m_hoffmann.meditate.util.NOTIFICATION_REQUEST_CODE
import com.gitlab.j_m_hoffmann.meditate.util.SECOND
import com.gitlab.j_m_hoffmann.meditate.util.midnight
import com.gitlab.j_m_hoffmann.meditate.widget.StreakWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

const val FIVE_MINUTES: Long = 5 * MINUTE

class SessionViewModel @Inject constructor(
    private val app: Context,
    private val repository: SessionRepository
) :
    ViewModel() {

    private val alarmManager = app.getSystemService<AlarmManager>()

    private val defaultDelay = app.getString(default_delay)

    private val keyLastSession = app.getString(key_last_session)
    private val keySessionDelay = app.getString(key_session_delay)
    private val keySessionLength = app.getString(key_session_length)
    private val keyStreakExpires = app.getString(key_streak_expires)
    private val keyStreakLongest = app.getString(key_streak_longest)
    private val keyStreakValue = app.getString(key_streak_value)

    private val notificationIntent = Intent(app, SessionEndedReceiver::class.java)

    private val notificationPendingIntent = PendingIntent.getBroadcast(
        app,
        NOTIFICATION_REQUEST_CODE,
        notificationIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    private val preferences = PreferenceManager.getDefaultSharedPreferences(app)

    //region Variables

    private var delayTimer: CountDownTimer? = null

    private var sessionLength = preferences.getLong(keySessionLength, FIVE_MINUTES)

    private var timer: CountDownTimer? = null

    //endregion

    //region LiveData

    val decrementEnabled: LiveData<Boolean>
        get() = _decrementEnabled
    private val _decrementEnabled = MutableLiveData(true)

    val delayTimeRemaining: LiveData<Long>
        get() = _delayTimeRemaining
    private val _delayTimeRemaining = MutableLiveData(0L)

    val delayTimeVisible: LiveData<Boolean>
        get() = _delayTimeVisible
    private val _delayTimeVisible = MutableLiveData(false)

    val state: LiveData<State>
        get() = _state
    private val _state = MutableLiveData(Ended)

    val sessionPaused: LiveData<Boolean>
        get() = _sessionPaused
    private val _sessionPaused = MutableLiveData(false)

    private val _currentStreak = MutableLiveData(preferences.getInt(keyStreakValue, 0))

    val currentStreak =
        Transformations.map(_currentStreak) { it.toPlural(R.plurals.days_of_meditation, R.string.empty, app) }

    val timeRemaining: LiveData<Long>
        get() = _timeRemaining
    private val _timeRemaining = MutableLiveData(sessionLength)

    //endregion

    //region PublicFunctions

    fun decrementDuration() {
        sessionLength -= FIVE_MINUTES

        if (sessionLength <= FIVE_MINUTES) {
            sessionLength = FIVE_MINUTES
            _decrementEnabled.value = false
        }

        _timeRemaining.value = sessionLength
    }

    fun endSession() {
        cancelAlarm()
        cancelDelayTimer()
        cancelTimer()

        if (_delayTimeRemaining.value!! > 0L) {
            resetSession()
        } else {
            _state.value = Aborted
        }
    }

    fun incrementDuration() {
        sessionLength += FIVE_MINUTES
        _decrementEnabled.value = true
        _timeRemaining.value = sessionLength
    }

    fun pauseOrResumeSession() = if (_sessionPaused.value!!) resumeSession() else pauseSession()

    fun resetSession() {
        _state.value = Ended
        _timeRemaining.value = sessionLength
    }

    fun saveSession() {
        updateMeditationStreak()
        persistSession(sessionLength - timeRemaining.value!!)
        resetSession()
    }

    fun startSession() {
        _state.value = InProgress

        preferences.edit { putLong(keySessionLength, sessionLength) }

        val sessionDelay = preferences.getString(keySessionDelay, defaultDelay)!!.toLong()

        startTimers(sessionLength, sessionDelay)
    }
    //endregion

    //region PrivateFunctions
    private fun cancelAlarm() = alarmManager?.cancel(notificationPendingIntent)

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
        startTimers(_timeRemaining.value!!, _delayTimeRemaining.value!!)
    }

    private fun persistSession(length: Long) = CoroutineScope(Dispatchers.Default).launch {
        repository.insert(Session(System.currentTimeMillis(), length))
    }

    private fun startTimers(duration: Long, delay: Long) {

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
                updateMeditationStreak()
                persistSession(sessionLength)
                resetSession()
            }

        }

        if (delay > 0L) {
            _delayTimeRemaining.value = delay
            _delayTimeVisible.value = true

            delayTimer = object : CountDownTimer(delay, SECOND) {

                override fun onTick(millisUntilFinished: Long) {
                    if (millisUntilFinished >= SECOND) {
                        _delayTimeRemaining.value = millisUntilFinished
                    } else {
                        onFinish()
                    }
                }

                override fun onFinish() {
                    _delayTimeRemaining.value = 0
                    cancelDelayTimer()
                    startSessionTimer(duration)
                }
            }

            delayTimer?.start()
        } else {
            startSessionTimer(duration)
        }
    }

    private fun startSessionTimer(duration: Long) {

        alarmManager?.let {
            AlarmManagerCompat.setExactAndAllowWhileIdle(
                it,
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + duration,
                notificationPendingIntent
            )
        }

        timer?.start()
    }

    private fun updateMeditationStreak() {

        val lastSessionDate = preferences.getLong(keyLastSession, Long.MIN_VALUE) // no session saved

        val midnight = midnight()

        if (lastSessionDate < midnight) { // no session saved for today

            val newStreak = _currentStreak.value!! + 1

            _currentStreak.value = newStreak

            CoroutineScope(Dispatchers.IO).launch {
                val longestStreak = preferences.getInt(keyStreakLongest, 0)

                preferences.edit {
                    putInt(keyStreakValue, newStreak)

                    putLong(keyLastSession, System.currentTimeMillis())

                    putLong(keyStreakExpires, midnight(2))

                    if (newStreak > longestStreak) {
                        putInt(keyStreakLongest, newStreak)
                    }
                }

                app.updateWidget<StreakWidget>()
            }
        }
    }
    //endregion
}
