package com.gitlab.j_m_hoffmann.meditate

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.gitlab.j_m_hoffmann.meditate.R.string
import com.gitlab.j_m_hoffmann.meditate.ui.timer.TimerFragment.OnSessionProgressListener
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(), OnSessionProgressListener {

    private lateinit var navBar: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navBar = findViewById(R.id.nav_bar)

        navBar.setupWithNavController(findNavController(R.id.nav_host_fragment))

        val nightMode = PreferenceManager.getDefaultSharedPreferences(this).getString(
            getString(string.key_theme),
            getString(string.default_theme)
        )

        AppCompatDelegate.setDefaultNightMode(nightMode!!.toInt())
    }

    override fun disableNavigation() {
        navBar.visibility = View.GONE
    }

    override fun enableNavigation() {
        navBar.visibility = View.VISIBLE
    }
}
