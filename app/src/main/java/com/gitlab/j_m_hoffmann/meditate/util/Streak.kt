package com.gitlab.j_m_hoffmann.meditate.util

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.gitlab.j_m_hoffmann.meditate.MeditateWidget
import java.util.Calendar
import java.util.Calendar.HOUR_OF_DAY
import java.util.Calendar.MILLISECOND
import java.util.Calendar.MINUTE
import java.util.Calendar.SECOND
import java.util.Date
import java.util.GregorianCalendar

fun midnight(shift: Int = 0): Long {
    val midnight = GregorianCalendar()
    midnight.time = Date()
    midnight.set(HOUR_OF_DAY, 0)
    midnight.set(MINUTE, 0)
    midnight.set(SECOND, 0)
    midnight.set(MILLISECOND, 0)
    midnight.add(Calendar.DATE, shift)

    return midnight.timeInMillis
}

fun updateWidget(context: Context) {
    val widgetManager = AppWidgetManager.getInstance(context)
    val componentName = ComponentName(context, MeditateWidget::class.java)
    val ids = widgetManager.getAppWidgetIds(componentName)

    if (ids.isNotEmpty()) {
        val updateIntent = Intent(context, MeditateWidget::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        }

        context.sendBroadcast(updateIntent)
    }
}
