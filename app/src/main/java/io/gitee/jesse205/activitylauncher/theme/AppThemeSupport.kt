package io.gitee.jesse205.activitylauncher.theme

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import io.gitee.jesse205.activitylauncher.preferences.AppPreferences
import io.gitee.jesse205.activitylauncher.utils.ActivityCompat.WINDOW_HIERARCHY_TAG
import io.gitee.jesse205.activitylauncher.utils.ActivityListener
import io.gitee.jesse205.activitylauncher.utils.ScopedActivityListenerManager
import io.gitee.jesse205.activitylauncher.utils.WindowCompat
import io.gitee.jesse205.activitylauncher.utils.isHighPerformanceDeviceByOSVersion

object AppThemeSupport : ScopedActivityListenerManager<AppThemeSupport.AppThemeSupportActivityListener>() {
    private const val TAG = "AppThemeSupport"
    private const val STATE_KEY_THEME_ID = "themeId"

    override fun createActivityScopeListener(activity: Activity) = AppThemeSupportActivityListener(activity)

    /**
     * 丢弃非自定义View的状态，用于解决不同主题之间的 SavedState 可能不兼容从而引发的 ClassCastException
     */
    fun Bundle.discardNonViewState() {
        getBundle(WINDOW_HIERARCHY_TAG)?.let {
            it.keySet().filter { key -> key != WindowCompat.VIEWS_TAG }.forEach { key ->
                it.remove(key)
            }
        }
    }

    fun Activity.recreateIfThemeChanged() {
        val appTheme = getActivityScopeListener(this).appTheme
        if (appTheme.id != ThemeManager.getCurrentTheme().id) {
            recreate()
        }
    }

    class AppThemeSupportActivityListener(val activity: Activity) : ActivityListener,
        SharedPreferences.OnSharedPreferenceChangeListener {
        val appTheme: AppTheme = ThemeManager.getCurrentTheme()
        var atLeastOnceResume = false

        override fun onActivityPreCreate(activity: Activity, savedInstanceState: Bundle?) {
            if (savedInstanceState != null) {
                val previousTheme = savedInstanceState.getString(STATE_KEY_THEME_ID)?.let {
                    ThemeManager.getThemeById(it)
                }
                if (previousTheme != null && previousTheme.group != ThemeManager.getCurrentTheme().group) {
                    Log.i(TAG, "onActivityPreCreate: Discarded state of non-custom Views for $activity")
                    savedInstanceState.discardNonViewState()
                }
            }
            activity.apply {
                ThemeManager.applyTheme(this, appTheme)
            }
        }

        override fun onActivityCreate(activity: Activity, savedInstanceState: Bundle?) {
            if (isHighPerformanceDeviceByOSVersion) {
                AppPreferences.registerChangeListener(this)
            }
        }

        override fun onActivityResume(activity: Activity) {
            if (!isHighPerformanceDeviceByOSVersion) {
                AppPreferences.registerChangeListener(this)
            }
            activity.recreateIfThemeChanged()
            atLeastOnceResume = true
        }

        override fun onActivityPause(activity: Activity) {
            if (!isHighPerformanceDeviceByOSVersion) {
                AppPreferences.unregisterListener(this)
            }
        }

        override fun onActivityDestroy(activity: Activity) {
            if (isHighPerformanceDeviceByOSVersion) {
                AppPreferences.unregisterListener(this)
            }
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            outState.putString(STATE_KEY_THEME_ID, appTheme.id)
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
            if (key == AppPreferences.PREFERENCE_KEY_THEME) {
                // 为解决平行视界下 recreate 时窗口不附加到窗口管理器的问题，仅在至少一次恢复时 recreate
                if (atLeastOnceResume) {
                    activity.recreateIfThemeChanged()
                }
            }
        }
    }
}