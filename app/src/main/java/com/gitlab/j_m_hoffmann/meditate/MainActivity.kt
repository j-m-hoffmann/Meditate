package com.gitlab.j_m_hoffmann.meditate

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navBar: BottomNavigationView = findViewById(R.id.nav_bar)

        val navController = findNavController(R.id.nav_host_fragment)

/*
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_settings, R.id.navigation_timer, R.id.navigation_progress
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
*/

        navBar.setupWithNavController(navController)
    }
}
