package com.gitlab.j_m_hoffmann.meditate.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import androidx.preference.PreferenceManager
import com.gitlab.j_m_hoffmann.meditate.MainActivity
import com.gitlab.j_m_hoffmann.meditate.R
import com.gitlab.j_m_hoffmann.meditate.R.id.widget_text
import com.gitlab.j_m_hoffmann.meditate.R.string.default_widget_color
import com.gitlab.j_m_hoffmann.meditate.R.string.key_streak_value
import com.gitlab.j_m_hoffmann.meditate.R.string.key_widget_color
import com.gitlab.j_m_hoffmann.meditate.extensions.toPlural

const val WIDGET_REQUEST_CODE = 2

class StreakWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {

        if (appWidgetIds.isNotEmpty()) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)

            val days = preferences.getInt(context.getString(key_streak_value), 0)

            val widgetText = days.toPlural(R.plurals.days_of_meditation, R.string.widget_text_default, context)

            val widgetTextColor =
                preferences.getString(context.getString(key_widget_color), context.getString(default_widget_color))

            val pendingIntent = Intent(context, MainActivity::class.java).let { intent ->
                PendingIntent.getActivity(context, WIDGET_REQUEST_CODE, intent, 0)
            }

            val streakWidget = RemoteViews(context.packageName, R.layout.widget).apply {
                setTextColor(widget_text, Color.parseColor(widgetTextColor))
                setTextViewText(widget_text, widgetText)
                setOnClickPendingIntent(widget_text, pendingIntent)
            }
            appWidgetIds.forEach { appWidgetManager.updateAppWidget(it, streakWidget) }
        }
    }
}

