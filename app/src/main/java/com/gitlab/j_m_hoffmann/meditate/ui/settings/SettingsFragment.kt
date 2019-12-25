package com.gitlab.j_m_hoffmann.meditate.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference.OnPreferenceChangeListener
import androidx.preference.PreferenceFragmentCompat
import com.gitlab.j_m_hoffmann.meditate.R

class SettingsFragment : PreferenceFragmentCompat() {

    private var themePreference: ListPreference? = null

    private val themeChangeListener = OnPreferenceChangeListener { _, newTheme ->
        AppCompatDelegate.setDefaultNightMode((newTheme as String).toInt())
        true
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        themePreference = findPreference(getString(R.string.key_theme))
    }

    override fun onResume() {
        super.onResume()
        themePreference?.onPreferenceChangeListener = themeChangeListener
    }

    override fun onPause() {
        super.onPause()
        themePreference?.onPreferenceChangeListener = null
    }
}
