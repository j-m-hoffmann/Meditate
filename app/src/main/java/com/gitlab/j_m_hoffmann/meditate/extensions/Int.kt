package com.gitlab.j_m_hoffmann.meditate.extensions

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes

fun Int.toPlural(@PluralsRes plural: Int, @StringRes default: Int, context: Context): String {
    return when (this) {
        0 -> context.getString(default)
        else -> {
            val quantityString = context.resources.getQuantityString(plural, this, this)

            String.format(quantityString, context.integerFormat().format(this))
        }
    }
}
