package io.gitee.jesse205.activitylauncher.theme

import android.app.Activity
import io.gitee.jesse205.activitylauncher.R
import io.gitee.jesse205.activitylauncher.preferences.AppPreferences

object ThemeManager {
    private const val PREF_THEME_KEY = "app_theme"

    fun getCurrentTheme(): AppTheme {
        return AppPreferences.themeId?.let { ThemeRegistry.getThemeById(it) }
            ?: ThemeRegistry.getDefaultTheme()
    }

    fun setTheme(themeId: String) {
        if (ThemeRegistry.isThemeCompatible(themeId)) {
            AppPreferences.themeId = themeId
        }
    }

    fun applyTheme(activity: Activity, appTheme: AppTheme = getCurrentTheme()) {
        activity.apply {
            theme.applyStyle(R.style.ThemeReset_ActivityLauncher, true)
            setTheme(appTheme.style)
            theme.applyStyle(appTheme.overlayStyle, true)
        }
    }
}