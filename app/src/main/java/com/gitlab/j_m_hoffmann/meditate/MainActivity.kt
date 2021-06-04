package com.gitlab.j_m_hoffmann.meditate

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.gitlab.j_m_hoffmann.meditate.R.string.default_theme
import com.gitlab.j_m_hoffmann.meditate.R.string.key_theme
import com.gitlab.j_m_hoffmann.meditate.databinding.ActivityMainBinding
import com.gitlab.j_m_hoffmann.meditate.extensions.updateWidget
import com.gitlab.j_m_hoffmann.meditate.ui.session.SessionViewModel
import com.gitlab.j_m_hoffmann.meditate.ui.session.State.Aborted
import com.gitlab.j_m_hoffmann.meditate.ui.session.State.Ended
import com.gitlab.j_m_hoffmann.meditate.ui.session.State.InProgress
import com.gitlab.j_m_hoffmann.meditate.widget.StreakWidget
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val sessionViewModel by viewModels<SessionViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.navBar.setupWithNavController(findNavController(R.id.nav_host_fragment))

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)

        val nightMode = preferences.getString(getString(key_theme), getString(default_theme))

        sessionViewModel.state.observe(this) {
            @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
            when (it) {
                Aborted -> {
                    binding.navBar.visibility = View.GONE
                }
                Ended -> {
                    binding.navBar.visibility = View.VISIBLE
                    applicationContext.updateWidget<StreakWidget>()
                }
                InProgress -> {
                    binding.navBar.visibility = View.GONE
                    Snackbar.make(binding.navBar, R.string.concentrate, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }
}