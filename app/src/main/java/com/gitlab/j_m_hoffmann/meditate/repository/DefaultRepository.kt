package com.gitlab.j_m_hoffmann.meditate.repository

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.gitlab.j_m_hoffmann.meditate.R
import com.gitlab.j_m_hoffmann.meditate.R.string
import com.gitlab.j_m_hoffmann.meditate.extensions.updateWidget
import com.gitlab.j_m_hoffmann.meditate.repository.db.Dao
import com.gitlab.j_m_hoffmann.meditate.repository.db.Session
import com.gitlab.j_m_hoffmann.meditate.util.MINUTE
import com.gitlab.j_m_hoffmann.meditate.widget.StreakWidget
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

const val DEFAULT_SESSION_LENGTH: Long = 15 * MINUTE

class DefaultRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: Dao
) : SessionRepository {

    override suspend fun insert(session: Session) = dao.insert(session)

    override suspend fun countSessions(): Int = dao.countSessions()

    override suspend fun durationAverage(): Long = dao.durationAverage() ?: 0

    override suspend fun durationLongest(): Long = dao.durationLongest() ?: 0

    override suspend fun durationTotal(): Long = dao.durationTotal() ?: 0

    override suspend fun lastSessionDate(): Long = dao.lastSessionDate() ?: 0

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    private val KEY_STREAK_VALUE = context.getString(R.string.key_streak_value)

    override val currentStreak: Int
        get() = preferences.getInt(KEY_STREAK_VALUE, 0)

    override val doNotDisturb: Boolean
        get() = preferences.getBoolean(context.getString(string.key_dnd), false)

    override fun updateStreak(days: Int, expiryEpochSecond: Long) {
        preferences.edit(commit = true) {
            putInt(KEY_STREAK_VALUE, days)
            putLong(context.getString(R.string.key_streak_expires), expiryEpochSecond)
        }
        if (days > longestStreak) {
            longestStreak = days
        }
        context.updateWidget<StreakWidget>()
    }

    private val KEY_STREAK_LONGEST = "key_streak_longest"
    override var longestStreak: Int
        get() = preferences.getInt(KEY_STREAK_LONGEST, 0)
        set(value) = preferences.edit(commit = true) { putInt(KEY_STREAK_LONGEST, value) }

    override val sessionDelay: Long
        get() {
            val keySessionDelay = context.getString(R.string.key_session_delay)
            val defaultDelay = context.getString(R.string.default_delay)
            return preferences.getString(keySessionDelay, defaultDelay)!!.toLong()
        }

    private val KEY_SESSION_LENGTH = "key_session_length"
    override var sessionLength: Long
        get() = preferences.getLong(KEY_SESSION_LENGTH, DEFAULT_SESSION_LENGTH)
        set(value) = preferences.edit { putLong(KEY_SESSION_LENGTH, value) }
}
