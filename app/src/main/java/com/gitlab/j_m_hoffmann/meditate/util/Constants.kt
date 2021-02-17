package com.gitlab.j_m_hoffmann.meditate.util

const val SECOND: Long = 1_000L
const val MINUTE: Long = 60 * SECOND
const val HOUR: Long = 60 * MINUTE
const val DAY: Long = 24 * HOUR

const val DEFAULT_DELAY = 30 * SECOND

const val NOTIFICATION_ID = 0
const val NOTIFICATION_REQUEST_CODE = 1
const val WIDGET_REQUEST_CODE = 2

const val STREAK_RESET_WORKER = "com.gitlab.j_m_hoffmann.meditate.streak_reset_worker"

const val KEY_LAST_SESSION = "key_last_session"
const val KEY_SESSION_DELAY = "key_session_delay"
const val KEY_SESSION_LENGTH = "key_session_length"
const val KEY_STREAK_EXPIRES = "key_streak_expires"
const val KEY_STREAK_LONGEST = "key_streak_longest"
const val KEY_STREAK_VALUE = "key_streak_value"
const val KEY_WIDGET_COLOR = "key_widget_color"
