package com.gitlab.j_m_hoffmann.meditate.ui.session

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.os.SystemClock
import androidx.core.app.AlarmManagerCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.gitlab.j_m_hoffmann.meditate.MeditateApplication
import com.gitlab.j_m_hoffmann.meditate.R
import com.gitlab.j_m_hoffmann.meditate.extensions.toPlural
import com.gitlab.j_m_hoffmann.meditate.extensions.updateWidget
import com.gitlab.j_m_hoffmann.meditate.receiver.SessionEndedReceiver
import com.gitlab.j_m_hoffmann.meditate.repository.SessionRepository
import com.gitlab.j_m_hoffmann.meditate.repository.db.Session
import com.gitlab.j_m_hoffmann.meditate.ui.session.State.Aborted
import com.gitlab.j_m_hoffmann.meditate.ui.session.State.Ended
import com.gitlab.j_m_hoffmann.meditate.ui.session.State.InProgress
import com.gitlab.j_m_hoffmann.meditate.util.MINUTE
import com.gitlab.j_m_hoffmann.meditate.util.SECOND
import com.gitlab.j_m_hoffmann.meditate.widget.StreakWidget
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

const val FIVE_MINUTES: Long = 5 * MINUTE
const val NOTIFICATION_REQUEST_CODE = 1

@HiltViewModel
class SessionViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val repository: SessionRepository
) : AndroidViewModel(context as Application) {

    //region Values

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

    private var lastSessionEpochSecond = 0L

    private var sessionLength = repository.sessionLength

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

    private val _currentStreak = MutableLiveData(repository.currentStreak)

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

    fun abortSession() {
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

    fun abortAndSave() = saveAndReset(sessionLength - timeRemaining.value!!)

    fun saveAndReset(duration: Long) {
        updateMeditationStreak()

        CoroutineScope(Dispatchers.IO).launch {
            repository.insert(Session(sessionBegin.toEpochSecond(zoneOffset), duration))
        }
        resetSession()
    }

    fun startSession() {
        sessionBegin = LocalDateTime.now(zoneId)

        viewModelScope.launch(Dispatchers.IO) { lastSessionEpochSecond = repository.lastSessionDate() }

        _state.value = InProgress

        repository.sessionLength = sessionLength

        startTimers(sessionLength, repository.sessionDelay)
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

        val durationWithOffset = duration + SECOND

        sessionTimer = object : CountDownTimer(durationWithOffset, SECOND) {

            override fun onTick(millisUntilFinished: Long) {
                if (millisUntilFinished >= SECOND) {
                    _timeRemaining.value = millisUntilFinished
                } else {
                    onFinish()
                }
            }

            override fun onFinish() {
                cancelSessionTimer()
                saveAndReset(sessionLength)
            }
        }

        fun startSessionTimer(duration: Long) {
            sessionTimer?.start()
            alarmManager?.let {
                AlarmManagerCompat.setExactAndAllowWhileIdle(
                    it,
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + duration,
                    sessionEndedIntent
                )
            }
        }

        if (delay > 0L) {
            val delayWithOffset = delay + SECOND

            delayTimer = object : CountDownTimer(delayWithOffset, SECOND) {

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
                    startSessionTimer(durationWithOffset)
                }
            }

            _delayTimeRemaining.value = delayWithOffset
            delayTimer?.start()
            _delayTimeVisible.value = true
        } else {
            startSessionTimer(durationWithOffset)
        }
    }

    private fun updateMeditationStreak() {
        val midnight = LocalDate.now(zoneId).atStartOfDay()
        val lastSessionDate = LocalDateTime.ofEpochSecond(lastSessionEpochSecond, 0, zoneOffset)

        if (lastSessionDate.isBefore(midnight)) {
            val newStreak = _currentStreak.value!! + 1
            _currentStreak.value = newStreak

            CoroutineScope(Dispatchers.IO).launch {
                repository.updateStreak(newStreak, midnight.plusDays(2).toEpochSecond(zoneOffset))
            }
            getApplication<MeditateApplication>().updateWidget<StreakWidget>()
        }
    }
    //endregion
}
