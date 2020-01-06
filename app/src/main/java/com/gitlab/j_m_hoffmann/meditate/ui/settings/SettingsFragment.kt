package com.gitlab.j_m_hoffmann.meditate.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference.OnPreferenceChangeListener
import androidx.preference.PreferenceFragmentCompat
import com.gitlab.j_m_hoffmann.meditate.R
import com.gitlab.j_m_hoffmann.meditate.R.string.key_theme
import com.gitlab.j_m_hoffmann.meditate.R.string.key_widget_color
import com.gitlab.j_m_hoffmann.meditate.util.updateWidget

class SettingsFragment : PreferenceFragmentCompat() {

    private var themePreference: ListPreference? = null

    private val themeChangeListener = OnPreferenceChangeListener { _, newTheme ->
        AppCompatDelegate.setDefaultNightMode((newTheme as String).toInt())
        true
    }

    private var widgetColorPreference: ListPreference? = null

    private val widgetColorChangeListener = OnPreferenceChangeListener { _, _ ->
        updateWidget(activity!!.applicationContext)
        true
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        themePreference = findPreference(getString(key_theme))
        widgetColorPreference = findPreference(getString(key_widget_color))
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
}
