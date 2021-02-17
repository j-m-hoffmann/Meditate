package com.gitlab.j_m_hoffmann.meditate

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentResolver.SCHEME_ANDROID_RESOURCE
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.getSystemService
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.gitlab.j_m_hoffmann.meditate.R.raw.metal_gong_by_dianakc
import com.gitlab.j_m_hoffmann.meditate.R.string.default_theme
import com.gitlab.j_m_hoffmann.meditate.R.string.key_theme
import com.gitlab.j_m_hoffmann.meditate.R.string.notification_channel_id
import com.gitlab.j_m_hoffmann.meditate.R.string.notification_channel_name
import com.gitlab.j_m_hoffmann.meditate.databinding.ActivityMainBinding
import com.gitlab.j_m_hoffmann.meditate.extensions.updateWidget
import com.gitlab.j_m_hoffmann.meditate.ui.session.SessionViewModel
import com.gitlab.j_m_hoffmann.meditate.ui.session.State.Ended
import com.gitlab.j_m_hoffmann.meditate.ui.session.State.InProgress
import com.gitlab.j_m_hoffmann.meditate.widget.StreakWidget
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

class MainActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val sessionViewModel by viewModels<SessionViewModel> { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.navBar.setupWithNavController(findNavController(R.id.nav_host_fragment))

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)

        val nightMode = preferences.getString(getString(key_theme), getString(default_theme))

        AppCompatDelegate.setDefaultNightMode(nightMode!!.toInt())

        sessionViewModel.state.observe(this, Observer { state ->
            when (state) {
                InProgress -> {
                    binding.navBar.visibility = View.GONE
                    Snackbar.make(binding.navBar, R.string.concentrate, Snackbar.LENGTH_LONG).show()
                }
                Ended -> {
                    binding.navBar.visibility = View.VISIBLE
                    applicationContext.updateWidget<StreakWidget>()
                }
                else -> binding.navBar.visibility = View.VISIBLE
            }
        })

        if (VERSION.SDK_INT >= VERSION_CODES.O) createNotificationChannel()
    }

    @RequiresApi(VERSION_CODES.O)
    private fun createNotificationChannel() {
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

        getSystemService<NotificationManager>()?.createNotificationChannel(channel)
    }
}