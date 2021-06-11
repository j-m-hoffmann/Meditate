package com.gitlab.j_m_hoffmann.meditate.ui.session

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.CountDownTimer
import android.os.SystemClock
import android.view.View
import androidx.core.app.AlarmManagerCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitlab.j_m_hoffmann.meditate.receiver.SessionEndedReceiver
import com.gitlab.j_m_hoffmann.meditate.repository.SessionRepository
import com.gitlab.j_m_hoffmann.meditate.repository.db.Session
import com.gitlab.j_m_hoffmann.meditate.ui.session.Session.Aborted
import com.gitlab.j_m_hoffmann.meditate.ui.session.Session.Ended
import com.gitlab.j_m_hoffmann.meditate.ui.session.Session.Started
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

const val FIVE_MINUTES: Long = 5 * MINUTE
const val NOTIFICATION_REQUEST_CODE = 1

@Suppress("UNUSED_PARAMETER")
@HiltViewModel
class SessionViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val repository: SessionRepository
) : ViewModel() {

    //region Values

    private val audioManager = context.getSystemService<AudioManager>() as AudioManager

    private val alarmManager = context.getSystemService<AlarmManager>()

    private val isAllowedToMute = if (Build.VERSION.SDK_INT >= 23) {
        context.getSystemService<NotificationManager>()!!.isNotificationPolicyAccessGranted
    } else {
        true
    }

    private val ringerModeBeforeSession = audioManager.ringerMode

    private val sessionEndedIntent = PendingIntent.getBroadcast(
        context,
        NOTIFICATION_REQUEST_CODE,
        Intent(context, SessionEndedReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT
    )

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

    val session: LiveData<com.gitlab.j_m_hoffmann.meditate.ui.session.Session>
        get() = _session
    private val _session = MutableLiveData(Ended)

    val sessionPaused: LiveData<Boolean>
        get() = _sessionPaused
    private val _sessionPaused = MutableLiveData(false)

    val currentStreak: LiveData<Int>
        get() = _currentStreak
    private val _currentStreak = MutableLiveData(repository.currentStreak)

    val timeRemaining: LiveData<Long>
        get() = _timeRemaining
    private val _timeRemaining = MutableLiveData(sessionLength)

    //endregion

    //region PublicFunctions

    fun decrementDuration(view: View) {
        sessionLength -= FIVE_MINUTES

        if (sessionLength <= FIVE_MINUTES) {
            sessionLength = FIVE_MINUTES
            _decrementEnabled.value = false
        }

        _timeRemaining.value = sessionLength
    }

    fun abortSession(view: View) {
        alarmManager?.cancel(sessionEndedIntent)
        cancelSessionTimer()

        if (_delayTimeRemaining.value!! > 0L) { // If session did not begin
            cancelDelayTimer()
            resetSession()
        } else {
            _session.value = Aborted // Enables saving or discarding the session
        }
    }

    fun discardSession(view: View) = resetSession()

    fun incrementDuration(view: View) {
        sessionLength += FIVE_MINUTES
        _decrementEnabled.value = true
        _timeRemaining.value = sessionLength
    }

    fun pauseOrResumeSession(view: View) {
        if (sessionPaused.value == false) {
            _sessionPaused.value = true
            alarmManager?.cancel(sessionEndedIntent)
            cancelSessionTimer()
            delayTimer?.run { cancelDelayTimer() }
        } else {
            _sessionPaused.value = false
            startTimers(_timeRemaining.value!!, _delayTimeRemaining.value!!)
        }
    }

    fun abortAndSave(view: View) = saveAndReset(sessionLength - timeRemaining.value!!)

    fun saveAndReset(duration: Long) {
        updateMeditationStreak()

        CoroutineScope(Dispatchers.IO).launch {
            repository.insert(Session(sessionBegin.toEpochSecond(zoneOffset), duration))
        }
        resetSession()
    }

    fun startSession(view: View) {
        sessionBegin = LocalDateTime.now(zoneId)

        viewModelScope.launch(Dispatchers.IO) { lastSessionEpochSecond = repository.lastSessionDate() }

        _session.value = Started

        repository.sessionLength = sessionLength

        if (repository.doNotDisturb && isAllowedToMute) {
            audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
        }

        val startOffset = SECOND
        startTimers(sessionLength + startOffset, repository.sessionDelay + startOffset)
    }
    //endregion

    //region PrivateFunctions
    private fun cancelDelayTimer() {
        delayTimer?.cancel()
        _delayTimeVisible.value = false
        delayTimer = null
    }

    private fun cancelSessionTimer() {
        sessionTimer?.cancel()
        sessionTimer = null
    }

    private fun resetSession() {
        _session.value = Ended
        _timeRemaining.value = sessionLength

        if (repository.doNotDisturb && isAllowedToMute) {
            audioManager.ringerMode = ringerModeBeforeSession
        }
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

            _delayTimeRemaining.value = delay
            delayTimer?.start()
            _delayTimeVisible.value = true
        } else {
            startSessionTimer(duration)
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
        }
    }
    //endregion
}
