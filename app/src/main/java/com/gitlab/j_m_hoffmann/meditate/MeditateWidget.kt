package com.gitlab.j_m_hoffmann.meditate

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import androidx.preference.PreferenceManager
import com.gitlab.j_m_hoffmann.meditate.R.id.widget_text
import com.gitlab.j_m_hoffmann.meditate.R.string.default_widget_color
import com.gitlab.j_m_hoffmann.meditate.R.string.key_streak_value
import com.gitlab.j_m_hoffmann.meditate.R.string.key_widget_color
import com.gitlab.j_m_hoffmann.meditate.R.string.widget_text_default
import com.gitlab.j_m_hoffmann.meditate.ui.extensions.integerFormat

class MeditateWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, id)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {

    val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    val days = preferences.getInt(context.getString(key_streak_value), 0)

    val widgetText = when (days) {
        0 -> context.getString(widget_text_default)
        else -> {
            val quantityString = context.resources.getQuantityString(R.plurals.days_of_meditation, days, days)

            String.format(quantityString, context.integerFormat().format(days))
        }
    }

    val pendingIntent = Intent(context, MainActivity::class.java).let { intent ->
        PendingIntent.getActivity(context, 0, intent, 0)
    }

    val color = preferences.getString(context.getString(key_widget_color), context.getString(default_widget_color))

    val views = RemoteViews(context.packageName, R.layout.widget).apply {
        setTextColor(widget_text, Color.parseColor(color))
        setTextViewText(widget_text, widgetText)
        setOnClickPendingIntent(widget_text, pendingIntent)
    }

    appWidgetManager.updateAppWidget(appWidgetId, views)
}