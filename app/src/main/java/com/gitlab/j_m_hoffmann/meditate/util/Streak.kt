package com.gitlab.j_m_hoffmann.meditate.util

import java.util.Calendar
import java.util.Calendar.HOUR_OF_DAY
import java.util.Calendar.MILLISECOND
import java.util.Calendar.MINUTE
import java.util.Calendar.SECOND
import java.util.Date
import java.util.GregorianCalendar

fun midnight(daysToAdd: Int = 0): Long {
    val midnight = GregorianCalendar()
    midnight.time = Date()
    midnight.set(HOUR_OF_DAY, 0)
    midnight.set(MINUTE, 0)
    midnight.set(SECOND, 0)
    midnight.set(MILLISECOND, 0)
    midnight.add(Calendar.DATE, daysToAdd)

    return midnight.timeInMillis
}
