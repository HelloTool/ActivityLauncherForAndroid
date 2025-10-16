@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package io.gitee.jesse205.activitylauncher.features.settings

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.preference.CheckBoxPreference
import android.preference.ListPreference
import android.preference.Preference
import androidx.annotation.RequiresApi
import io.gitee.jesse205.activitylauncher.R
import io.gitee.jesse205.activitylauncher.app.BasePreferenceActivity
import io.gitee.jesse205.activitylauncher.preferences.AppPreferences
import io.gitee.jesse205.activitylauncher.theme.AppTheme
import io.gitee.jesse205.activitylauncher.theme.DayNightTheme
import io.gitee.jesse205.activitylauncher.theme.ThemeManager
import io.gitee.jesse205.activitylauncher.utils.findPreferenceCompat
import io.gitee.jesse205.activitylauncher.utils.isActionBarSupported

class SettingsActivity : BasePreferenceActivity() {
    private lateinit var themePreference: ListPreference
    private lateinit var darkModePreference: ListPreference
    private lateinit var darkActionBarPreference: CheckBoxPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isActionBarSupported) {
            setupActionBar()
        }
        addPreferencesFromResource(R.xml.settings)
        setupPreferences()
    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    private fun setupActionBar() {
        getActionBar()?.apply {
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupPreferences() {
        themePreference = findPreferenceCompat<ListPreference>(AppPreferences.PREFERENCE_THEME)!!.apply {
            entries = ThemeManager.themes.map { it.getDisplayName(this@SettingsActivity) }.toTypedArray()
            entryValues = ThemeManager.themes.map { it.id }.toTypedArray()
            value = ThemeManager.getCurrentTheme().id
            onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                val newTheme = ThemeManager.getThemeById(newValue as String) ?: return@OnPreferenceChangeListener false
                onUpdateTheme(newTheme)
                true
            }
        }
        darkModePreference =
            findPreferenceCompat<ListPreference>(AppPreferences.PREFERENCE_THEME_DARK_MODE)!!.apply {
                summary = entry
                onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                    onUpdateDarkMode(newValue as String)
                    true
                }
            }

        darkActionBarPreference =
            findPreferenceCompat<CheckBoxPreference>(AppPreferences.PREFERENCE_THEME_DARK_ACTION_BAR)!!

        onUpdateTheme(ThemeManager.getCurrentTheme())
    }

    fun onUpdateTheme(newTheme: AppTheme) {
        themePreference.apply {
            summary = newTheme.getDisplayName(this@SettingsActivity)
        }
        darkModePreference.apply {
            isEnabled = newTheme is DayNightTheme
        }
        updateDarkActionBarEnabled(newTheme = newTheme)
        ThemeManager.setTheme(newTheme.id)
    }

    fun onUpdateDarkMode(newDarkMode: String) {
        darkModePreference.apply {
            summary = entries[entryValues.indexOf(newDarkMode)]
        }
        updateDarkActionBarEnabled(newDarkMode = newDarkMode)
    }

    fun updateDarkActionBarEnabled(
        newTheme: AppTheme = ThemeManager.getCurrentTheme(),
        newDarkMode: String = AppPreferences.darkMode
    ) {
        darkActionBarPreference.isEnabled = newTheme is DayNightTheme
                && newTheme.lightDarkActionBar != null
                && newDarkMode != AppPreferences.THEME_DARK_MODE_ON
    }

    companion object {

        fun launch(context: Context) {
            val intent = Intent(context, SettingsActivity::class.java)
            context.startActivity(intent)
        }
    }
}