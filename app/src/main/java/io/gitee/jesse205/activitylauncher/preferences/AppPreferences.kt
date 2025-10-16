@file:Suppress("DEPRECATION")

package io.gitee.jesse205.activitylauncher.preferences

import android.content.SharedPreferences
import android.preference.PreferenceManager
import io.gitee.jesse205.activitylauncher.app.ActivityLauncherApp

object AppPreferences {
    const val PREFERENCE_THEME = "theme"
    const val PREFERENCE_THEME_DARK_ACTION_BAR = "theme_dark_action_bar"
    const val PREFERENCE_THEME_DARK_MODE = "theme_dark_mode"
    const val THEME_DARK_MODE_OFF = "off"
    const val THEME_DARK_MODE_ON = "on"
    const val THEME_DARK_MODE_SYSTEM = "system"

    private val sharedPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(ActivityLauncherApp.INSTANCE)

    var themeId: String?
        get() = sharedPreferences.getString(PREFERENCE_THEME, null)
        set(value) {
            sharedPreferences.edit()
                .putString(PREFERENCE_THEME, value)
                .apply()
        }

    var isDarkActionBar: Boolean
        get() = sharedPreferences.getBoolean(PREFERENCE_THEME_DARK_ACTION_BAR, false)
        set(value) {
            sharedPreferences.edit()
                .putBoolean(PREFERENCE_THEME_DARK_ACTION_BAR, value)
                .apply()
        }

    var darkMode: String
        get() = sharedPreferences.getString(PREFERENCE_THEME_DARK_MODE, THEME_DARK_MODE_SYSTEM)!!
        set(value) {
            sharedPreferences.edit()
                .putString(PREFERENCE_THEME_DARK_MODE, value)
                .apply()
        }

    fun registerChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
    }
}