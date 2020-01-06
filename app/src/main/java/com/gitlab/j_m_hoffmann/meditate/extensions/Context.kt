package com.gitlab.j_m_hoffmann.meditate.extensions

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION
import java.text.NumberFormat
import java.util.Locale

fun Context.integerFormat(): NumberFormat = NumberFormat.getIntegerInstance(locale())

fun Context.locale(): Locale {
    return if (VERSION.SDK_INT >= 24) {
        resources.configuration.locales[0]
    } else {
        @Suppress("DEPRECATION")
        resources.configuration.locale
    }
}

inline fun <reified W> Context.updateWidget() where W : AppWidgetProvider {
    val widgetManager = AppWidgetManager.getInstance(this)
    val componentName = ComponentName(this, W::class.java)
    val ids = widgetManager.getAppWidgetIds(componentName)

    if (ids.isNotEmpty()) {
        val updateIntent = Intent(this, W::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        }

        this.sendBroadcast(updateIntent)
    }
}
