@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package io.gitee.jesse205.activitylauncher.features.settings

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.preference.CheckBoxPreference
import android.preference.ListPreference
import android.preference.Preference
import android.view.MenuItem
import androidx.annotation.RequiresApi
import io.gitee.jesse205.activitylauncher.R
import io.gitee.jesse205.activitylauncher.app.BasePreferenceActivity
import io.gitee.jesse205.activitylauncher.preferences.AppPreferences
import io.gitee.jesse205.activitylauncher.theme.AppTheme
import io.gitee.jesse205.activitylauncher.theme.DayNightTheme
import io.gitee.jesse205.activitylauncher.theme.ThemeManager
import io.gitee.jesse205.activitylauncher.utils.findPreferenceCompat

class SettingsActivity : BasePreferenceActivity() {
    private lateinit var darkModePreference: ListPreference
    private lateinit var darkActionBarPreference: CheckBoxPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // 老版本中默认不调用 onBackPressed
        when (item.itemId) {
            item.itemId -> onBackPressed()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun setupPreferences() {
        findPreferenceCompat<ListPreference>(AppPreferences.PREFERENCE_THEME)!!.apply {
            entries = ThemeManager.themes.map { it.getDisplayName(this@SettingsActivity) }.toTypedArray()
            entryValues = ThemeManager.themes.map { it.id }.toTypedArray()
            val currentTheme = ThemeManager.getCurrentTheme()
            value = currentTheme.id
            summary = currentTheme.getDisplayName(this@SettingsActivity)
            onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                newValue as String
                val newTheme = ThemeManager.getThemeById(newValue) ?: return@OnPreferenceChangeListener false
                summary = newTheme.getDisplayName(this@SettingsActivity)
                ThemeManager.setTheme(newValue)
                updateThemeBasedEnabled(newTheme)
                true
            }
        }
        darkModePreference =
            findPreferenceCompat<ListPreference>(AppPreferences.PREFERENCE_THEME_DARK_MODE)!!.apply {
                summary = entry
                onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                    newValue as String
                    summary = entries[entryValues.indexOf(newValue)]
                    updateDarkModeBasedEnabled(newDarkMode = newValue)
                    true
                }
            }

        darkActionBarPreference =
            findPreferenceCompat<CheckBoxPreference>(AppPreferences.PREFERENCE_THEME_DARK_ACTION_BAR)!!

        updateThemeBasedEnabled()
    }

    fun updateThemeBasedEnabled(newTheme: AppTheme = ThemeManager.getCurrentTheme()) {
        darkModePreference.isEnabled = newTheme is DayNightTheme
        updateDarkModeBasedEnabled(newTheme)
    }

    fun updateDarkModeBasedEnabled(
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