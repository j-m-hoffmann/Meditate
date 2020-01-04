package com.gitlab.j_m_hoffmann.meditate

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentResolver.SCHEME_ANDROID_RESOURCE
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.gitlab.j_m_hoffmann.meditate.R.raw.metal_gong_by_dianakc
import com.gitlab.j_m_hoffmann.meditate.R.string.default_theme
import com.gitlab.j_m_hoffmann.meditate.R.string.key_theme
import com.gitlab.j_m_hoffmann.meditate.R.string.notification_channel_id
import com.gitlab.j_m_hoffmann.meditate.R.string.notification_channel_name
import com.gitlab.j_m_hoffmann.meditate.ui.timer.TimerFragment.OnSessionProgressListener
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(), OnSessionProgressListener {

    private lateinit var navBar: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navBar = findViewById(R.id.nav_bar)

        navBar.setupWithNavController(findNavController(R.id.nav_host_fragment))

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)

        val nightMode = preferences.getString(getString(key_theme), getString(default_theme))

        AppCompatDelegate.setDefaultNightMode(nightMode!!.toInt())

        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val sound = Uri.parse("$SCHEME_ANDROID_RESOURCE://${packageName}/$metal_gong_by_dianakc")

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val channel = NotificationChannel(
                getString(notification_channel_id),
                getString(notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setShowBadge(false)
                setSound(sound, audioAttributes)
            }

            val notificationManager = getSystemService(NotificationManager::class.java) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun hideNavigation() {
        navBar.visibility = View.GONE
    }

    override fun showNavigation() {
        navBar.visibility = View.VISIBLE
    }
}
