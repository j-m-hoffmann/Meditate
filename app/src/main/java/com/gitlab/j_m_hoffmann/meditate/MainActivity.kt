package com.gitlab.j_m_hoffmann.meditate

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.gitlab.j_m_hoffmann.meditate.R.id
import com.gitlab.j_m_hoffmann.meditate.ui.timer.TimerFragment.OnSessionChangeListener
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(), OnSessionChangeListener {

    lateinit var navBar: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navBar = findViewById(R.id.nav_bar)

        navBar.setupWithNavController(findNavController(id.nav_host_fragment))

    }

    override fun disableNavigation() {
        navBar.visibility = View.GONE
    }

    override fun enableNavigation() {
        navBar.visibility = View.VISIBLE
    }
}
