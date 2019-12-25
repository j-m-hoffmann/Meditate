package com.gitlab.j_m_hoffmann.meditate.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.preference.Preference.OnPreferenceChangeListener
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.gitlab.j_m_hoffmann.meditate.R

class SettingsFragment : PreferenceFragmentCompat() {

    private var nightMode: SwitchPreferenceCompat? = null

    private val nightModeListener = OnPreferenceChangeListener { _, newValue ->
        if (newValue as Boolean) {
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
        }
        true
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onResume() {
        super.onResume()
        nightMode = findPreference(getString(R.string.key_use_dark_theme))
        nightMode?.onPreferenceChangeListener = nightModeListener
    }

    override fun onPause() {
        super.onPause()
        nightMode?.onPreferenceChangeListener = null
    }
}
