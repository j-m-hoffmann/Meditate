package com.gitlab.j_m_hoffmann.meditate.ui.util

import android.text.format.DateUtils
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.gitlab.j_m_hoffmann.meditate.util.SECOND

@BindingAdapter("formatTime")
fun TextView.formatTime(milliseconds: Long) {
    val seconds = milliseconds / SECOND
    text = if (seconds > 59) DateUtils.formatElapsedTime(seconds) else seconds.toString()
}
