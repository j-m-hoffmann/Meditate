package com.gitlab.j_m_hoffmann.meditate.ui.session

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
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.preference.PreferenceManager
import com.gitlab.j_m_hoffmann.meditate.MeditateApplication
import com.gitlab.j_m_hoffmann.meditate.R
import com.gitlab.j_m_hoffmann.meditate.R.string.default_delay
import com.gitlab.j_m_hoffmann.meditate.R.string.key_last_session
import com.gitlab.j_m_hoffmann.meditate.R.string.key_session_delay
import com.gitlab.j_m_hoffmann.meditate.R.string.key_session_length
import com.gitlab.j_m_hoffmann.meditate.R.string.key_streak_expires
import com.gitlab.j_m_hoffmann.meditate.R.string.key_streak_longest
import com.gitlab.j_m_hoffmann.meditate.R.string.key_streak_value
import com.gitlab.j_m_hoffmann.meditate.db.Dao
import com.gitlab.j_m_hoffmann.meditate.db.Session
import com.gitlab.j_m_hoffmann.meditate.extensions.toPlural
import com.gitlab.j_m_hoffmann.meditate.extensions.updateWidget
import com.gitlab.j_m_hoffmann.meditate.receiver.SessionEndedReceiver
import com.gitlab.j_m_hoffmann.meditate.util.MINUTE
import com.gitlab.j_m_hoffmann.meditate.util.REQUEST_CODE
import com.gitlab.j_m_hoffmann.meditate.util.SECOND
import com.gitlab.j_m_hoffmann.meditate.util.midnight
import com.gitlab.j_m_hoffmann.meditate.widget.StreakWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val FIVE_MINUTES: Long = 5 * MINUTE

class SessionViewModel(val app: MeditateApplication, private val dao: Dao) : ViewModel() {

    private val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private val keySessionLength = app.getString(key_session_length)
    private val keyStreakValue = app.getString(key_streak_value)

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

    private val sessionDelay = preferences.getString(
        app.getString(key_session_delay),
        app.getString(default_delay)
    )!!.toLong()

    private var sessionLength = preferences.getLong(keySessionLength, FIVE_MINUTES)

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

    private val _streak = MutableLiveData(preferences.getInt(keyStreakValue, 0))

    val streak = Transformations.map(_streak) { it.toPlural(R.plurals.days_of_meditation, R.string.empty, app) }

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
        updateMeditationStreak()
        persistSession(sessionLength - timeRemaining.value!!)
        resetSession()
    }

    fun startSession() {
        _sessionInProgress.value = true

        preferences.edit { putLong(keySessionLength, sessionLength) }

        showEndAndPauseButtons()

        startTimers(sessionLength, sessionDelay)
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
        startTimers(_timeRemaining.value!!, _delayTimeRemaining.value!!)
    }

    private fun persistSession(length: Long) = CoroutineScope(Dispatchers.Default).launch {
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
                    startTimer(duration)
                }
            }

            delayTimer?.start()
        } else {
            startTimer(duration)
        }
    }

    private fun startTimer(duration: Long) {

        AlarmManagerCompat.setExactAndAllowWhileIdle(
            alarmManager,
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + duration,
            notificationPendingIntent
        )

        timer?.start()
    }

    private fun updateMeditationStreak() {

        val keyLastSession = app.getString(key_last_session)

        val lastSessionDate = preferences.getLong(keyLastSession, Long.MIN_VALUE) // no session saved

        val midnight = midnight()

        if (lastSessionDate < midnight) { // no session saved for today

            val newStreak = _streak.value!! + 1

            _streak.value = newStreak

            CoroutineScope(Dispatchers.IO).launch {
                val keyStreakLongest = app.getString(key_streak_longest)

                val longestStreak = preferences.getInt(keyStreakLongest, 0)

                preferences.edit {
                    putInt(keyStreakValue, newStreak)

                    putLong(keyLastSession, System.currentTimeMillis())

                    putLong(app.getString(key_streak_expires), midnight(2))

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
