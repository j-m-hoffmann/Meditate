package com.gitlab.j_m_hoffmann.meditate.ui.util

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gitlab.j_m_hoffmann.meditate.MeditateApplication
import com.gitlab.j_m_hoffmann.meditate.ui.progress.ProgressViewModel
import com.gitlab.j_m_hoffmann.meditate.ui.timer.TimerViewModel

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(private val app: MeditateApplication) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return with(modelClass) {
            val dao = app.database.dao
            when {
                isAssignableFrom(ProgressViewModel::class.java) -> ProgressViewModel(app, dao)
                isAssignableFrom(TimerViewModel::class.java) -> TimerViewModel(app, dao)
                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
    }
}

fun Fragment.getViewModelFactory(): ViewModelFactory =
    ViewModelFactory((requireContext().applicationContext as MeditateApplication))