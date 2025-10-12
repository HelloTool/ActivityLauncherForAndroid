@file:Suppress("DEPRECATION")

package io.gitee.jesse205.activitylauncher.preferences

import android.content.SharedPreferences
import android.preference.PreferenceManager
import io.gitee.jesse205.activitylauncher.app.ActivityLauncherApp

object AppPreferences {
    const val PREFERENCE_KEY_THEME = "theme"

    private val sharedPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(ActivityLauncherApp.INSTANCE)

    var themeId: String?
        get() = sharedPreferences.getString(PREFERENCE_KEY_THEME, null)
        set(value) {
            sharedPreferences.edit()
                .putString(PREFERENCE_KEY_THEME, value)
                .apply()
        }

    fun registerChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
    }
}