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
import com.gitlab.j_m_hoffmann.meditate.extensions.toPlural
import com.gitlab.j_m_hoffmann.meditate.receiver.SessionEndedReceiver
import com.gitlab.j_m_hoffmann.meditate.repository.SessionRepository
import com.gitlab.j_m_hoffmann.meditate.repository.db.Session
import com.gitlab.j_m_hoffmann.meditate.ui.session.State.Aborted
import com.gitlab.j_m_hoffmann.meditate.ui.session.State.Ended
import com.gitlab.j_m_hoffmann.meditate.ui.session.State.InProgress
import com.gitlab.j_m_hoffmann.meditate.util.MINUTE
import com.gitlab.j_m_hoffmann.meditate.util.SECOND
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import javax.inject.Inject

const val DEFAULT_DELAY = 30 * SECOND
const val FIVE_MINUTES: Long = 5 * MINUTE
const val NOTIFICATION_REQUEST_CODE = 1

@HiltViewModel
class SessionViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val repository: SessionRepository
) : ViewModel() {

    //region Values

    private val KEY_STREAK_EXPIRES = context.getString(R.string.key_streak_expires)
    private val KEY_STREAK_VALUE = context.getString(R.string.key_streak_value)
    private val KEY_LAST_SESSION = context.getString(R.string.key_last_session)
    private val KEY_SESSION_DELAY = context.getString(R.string.key_session_delay)
    private val KEY_SESSION_LENGTH = context.getString(R.string.key_session_length)
    private val KEY_STREAK_LONGEST = context.getString(R.string.key_streak_longest)

    private val alarmManager = context.getSystemService<AlarmManager>()

    private val sessionEndedIntent = PendingIntent.getBroadcast(
        context,
        NOTIFICATION_REQUEST_CODE,
        Intent(context, SessionEndedReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    private val zoneId = ZoneId.systemDefault()
    private val zoneOffset = OffsetDateTime.now(zoneId).offset

    //endregion

    //region Variables

    private var delayTimer: CountDownTimer? = null

    private var sessionLength = preferences.getLong(KEY_SESSION_LENGTH, FIVE_MINUTES)

    private var sessionBegin = LocalDateTime.now(zoneId)

    private var sessionTimer: CountDownTimer? = null

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

    private val _currentStreak = MutableLiveData(preferences.getInt(KEY_STREAK_VALUE, 0))

    val currentStreak = Transformations.map(_currentStreak) { days ->
        days.toPlural(R.plurals.days_of_meditation, R.string.empty, context)
    }

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
        alarmManager?.cancel(sessionEndedIntent)
        cancelDelayTimer()
        cancelSessionTimer()

        if (_delayTimeRemaining.value!! > 0L) { // If session did not begin
            resetSession()
        } else {
            _state.value = Aborted // Enables saving or discarding the session
        }
    }

    fun incrementDuration() {
        sessionLength += FIVE_MINUTES
        _decrementEnabled.value = true
        _timeRemaining.value = sessionLength
    }

    fun pauseOrResumeSession() {
        if (sessionPaused.value == false) {
            _sessionPaused.value = true
            alarmManager?.cancel(sessionEndedIntent)
            cancelSessionTimer()
            cancelDelayTimer()
        } else {
            _sessionPaused.value = false
            startTimers(_timeRemaining.value!!, _delayTimeRemaining.value!!)
        }
    }

    fun resetSession() {
        _state.value = Ended
        _timeRemaining.value = sessionLength
    }

    fun saveAndReset() {
        val duration = sessionLength - timeRemaining.value!!
        updateMeditationStreak()

        CoroutineScope(Dispatchers.IO).launch {
            repository.insert(Session(sessionBegin.toEpochSecond(zoneOffset), duration))
        }
        resetSession()
    }

    fun startSession() {
        sessionBegin = LocalDateTime.now(zoneId)

        _state.value = InProgress

        preferences.edit { putLong(KEY_SESSION_LENGTH, sessionLength) }

        val sessionDelay = preferences.getString(KEY_SESSION_DELAY, null)?.toLong() ?: DEFAULT_DELAY

        startTimers(sessionLength, sessionDelay)
    }
    //endregion

    //region PrivateFunctions
    private fun cancelDelayTimer() {
        delayTimer?.cancel()
        _delayTimeVisible.value = false
    }

    private fun cancelSessionTimer() {
        sessionTimer?.cancel()
        sessionTimer = null
    }

    private fun startTimers(duration: Long, delay: Long) {

        sessionTimer = object : CountDownTimer(duration, SECOND) {

            override fun onTick(millisUntilFinished: Long) {
                if (millisUntilFinished >= SECOND) {
                    _timeRemaining.value = millisUntilFinished
                } else {
                    onFinish()
                }
            }

            override fun onFinish() {
                cancelSessionTimer()
                saveAndReset()
            }
        }

        fun startSessionTimer(duration: Long) {
            alarmManager?.let {
                AlarmManagerCompat.setExactAndAllowWhileIdle(
                    it,
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + duration,
                    sessionEndedIntent
                )
            }
            sessionTimer?.start()
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

    private fun updateMeditationStreak() {
        val midnight = LocalDate.now(zoneId).atStartOfDay()
        val lastSessionEpochSecond = preferences.getLong(KEY_LAST_SESSION, 0) // no session saved
        val lastSessionDate = LocalDateTime.ofEpochSecond(lastSessionEpochSecond, 0, zoneOffset)

        if (lastSessionDate.isBefore(midnight)) {
            val newStreak = _currentStreak.value!! + 1
            _currentStreak.value = newStreak

            CoroutineScope(Dispatchers.IO).launch {
                val longestStreak = preferences.getInt(KEY_STREAK_LONGEST, 0)

                preferences.edit(commit = true) {
                    putInt(KEY_STREAK_VALUE, newStreak)
                    putLong(KEY_LAST_SESSION, sessionBegin.toEpochSecond(zoneOffset))
                    putLong(KEY_STREAK_EXPIRES, midnight.plusDays(2).toEpochSecond(zoneOffset))
                    if (newStreak > longestStreak) putInt(KEY_STREAK_LONGEST, newStreak)
                }
            }
        }
    }
    //endregion
}
