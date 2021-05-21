package com.gitlab.j_m_hoffmann.meditate.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference.OnPreferenceChangeListener
import androidx.preference.PreferenceFragmentCompat
import com.gitlab.j_m_hoffmann.meditate.R
import com.gitlab.j_m_hoffmann.meditate.extensions.updateWidget
import com.gitlab.j_m_hoffmann.meditate.widget.StreakWidget

class SettingsFragment : PreferenceFragmentCompat() {

    private var themePreference: ListPreference? = null
    private var widgetColorPreference: ListPreference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        themePreference = findPreference(getString(R.string.key_theme))
        widgetColorPreference = findPreference(getString(R.string.key_widget_color))
    }

    override fun onResume() {
        super.onResume()
        themePreference?.onPreferenceChangeListener = themeChangeListener
        widgetColorPreference?.onPreferenceChangeListener = widgetColorChangeListener
    }

    override fun onPause() {
        super.onPause()
        themePreference?.onPreferenceChangeListener = null
        widgetColorPreference?.onPreferenceChangeListener = null
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
