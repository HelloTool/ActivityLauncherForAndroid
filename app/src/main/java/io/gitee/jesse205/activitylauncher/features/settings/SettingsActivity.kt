@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package io.gitee.jesse205.activitylauncher.features.settings

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import androidx.annotation.RequiresApi
import io.gitee.jesse205.activitylauncher.R
import io.gitee.jesse205.activitylauncher.app.BasePreferenceActivity
import io.gitee.jesse205.activitylauncher.preferences.AppPreferences
import io.gitee.jesse205.activitylauncher.theme.ThemeManager
import io.gitee.jesse205.activitylauncher.theme.ThemeRegistry
import io.gitee.jesse205.activitylauncher.utils.findPreferenceCompat

class SettingsActivity : BasePreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setupActionBar()
        }
        addPreferencesFromResource(R.xml.settings)
        setupThemePreference()
    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    private fun setupActionBar() {
        getActionBar()?.apply {
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupThemePreference() {
        findPreferenceCompat<ListPreference>(AppPreferences.PREFERENCE_KEY_THEME)?.apply {
            ThemeRegistry.availableThemes

            entries = ThemeRegistry.availableThemes.map { it.getDisplayName(this@SettingsActivity) }.toTypedArray()
            entryValues = ThemeRegistry.availableThemes.map { it.id }.toTypedArray()
            val currentTheme = ThemeManager.getCurrentTheme()
            value = currentTheme.id
            summary = currentTheme.getDisplayName(this@SettingsActivity)
            onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                newValue as String
                summary == ThemeRegistry.getThemeById(newValue)?.getDisplayName(this@SettingsActivity)
                ThemeManager.setTheme(newValue)
                recreate()
                true
            }
        }
        findPreferenceCompat<Preference>(KEY_THEME_RESTART_TIP)?.apply {
            setVisible(false)
        }
    }

    companion object {
        const val KEY_THEME = "theme"
        const val KEY_THEME_RESTART_TIP = "theme_restart_tip"

        // 主题值常量
        const val THEME_GINGERBREAD_LIGHT = "gingerbread_light"
        const val THEME_GINGERBREAD_DARK = "gingerbread_dark"
        const val THEME_HOLO_LIGHT = "holo_light"
        const val THEME_HOLO_DARK = "holo_dark"
        const val THEME_MATERIAL_LIGHT = "material_light"
        const val THEME_MATERIAL_DARK = "material_dark"
        const val THEME_DEVICE_DEFAULT_LIGHT = "device_default_light"
        const val THEME_DEVICE_DEFAULT_DARK = "device_default_dark"
        const val THEME_DEVICE_SETTINGS = "device_settings"

        fun launch(context: Context) {
            val intent = Intent(context, SettingsActivity::class.java)
            context.startActivity(intent)
        }
    }
}