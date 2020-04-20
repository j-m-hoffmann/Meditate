package com.gitlab.j_m_hoffmann.meditate.extensions

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION
import androidx.core.content.getSystemService
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

inline fun <reified AWP : AppWidgetProvider> Context.updateWidget() {
    getSystemService<AppWidgetManager>()?.let { widgetManager ->
        val componentName = ComponentName(this, AWP::class.java)
        val ids = widgetManager.getAppWidgetIds(componentName)

        if (ids.isNotEmpty()) {
            val updateIntent = Intent(this, AWP::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }

            this.sendBroadcast(updateIntent)
        }
    }
}
