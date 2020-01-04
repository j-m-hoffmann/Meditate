package com.gitlab.j_m_hoffmann.meditate.ui.extensions

import android.app.Application
import android.os.Build.VERSION
import java.text.NumberFormat
import java.util.Locale

fun Application.integerFormat(): NumberFormat = NumberFormat.getIntegerInstance(locale())

fun Application.locale(): Locale {
    return if (VERSION.SDK_INT >= 24) {
        resources.configuration.locales[0]
    } else {
        @Suppress("DEPRECATION")
        resources.configuration.locale
    }
}
