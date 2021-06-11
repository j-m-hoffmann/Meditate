package com.gitlab.j_m_hoffmann.meditate.ui.settings

import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.getSystemService
import androidx.preference.ListPreference
import androidx.preference.Preference.OnPreferenceChangeListener
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.gitlab.j_m_hoffmann.meditate.R
import com.gitlab.j_m_hoffmann.meditate.extensions.updateWidget
import com.gitlab.j_m_hoffmann.meditate.widget.StreakWidget

class SettingsFragment : PreferenceFragmentCompat() {

    private var doNotDisturbPreference: SwitchPreference? = null
    private var themePreference: ListPreference? = null
    private var widgetColorPreference: ListPreference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        doNotDisturbPreference = findPreference(getString(R.string.key_dnd))
        themePreference = findPreference(getString(R.string.key_theme))
        widgetColorPreference = findPreference(getString(R.string.key_widget_color))
    }

    override fun onResume() {
        super.onResume()
        doNotDisturbPreference?.onPreferenceChangeListener = doNotDisturbChangeListener
        themePreference?.onPreferenceChangeListener = themeChangeListener
        widgetColorPreference?.onPreferenceChangeListener = widgetColorChangeListener
    }

    override fun onPause() {
        super.onPause()
        doNotDisturbPreference?.onPreferenceChangeListener = null
        themePreference?.onPreferenceChangeListener = null
        widgetColorPreference?.onPreferenceChangeListener = null
    }

    private val doNotDisturbChangeListener = OnPreferenceChangeListener { _, doNotDisturb ->
        if (doNotDisturb as Boolean && Build.VERSION.SDK_INT >= 23) {
            requireActivity().getSystemService<NotificationManager>()
                ?.isNotificationPolicyAccessGranted?.let { isGranted ->
                    if (!isGranted) startActivity(
                        Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                    )
                }
        }
        true
    }

    private val themeChangeListener = OnPreferenceChangeListener { _, newTheme ->
        AppCompatDelegate.setDefaultNightMode((newTheme as String).toInt())
        true
    }

    private val widgetColorChangeListener = OnPreferenceChangeListener { _, _ ->
        requireActivity().applicationContext.updateWidget<StreakWidget>()
        true
    }
}
