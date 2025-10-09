package io.gitee.jesse205.activitylauncher.theme

import android.app.Activity
import android.os.Bundle
import android.util.Log
import io.gitee.jesse205.activitylauncher.utils.ActivityCompat.WINDOW_HIERARCHY_TAG
import io.gitee.jesse205.activitylauncher.utils.ActivityListener
import io.gitee.jesse205.activitylauncher.utils.ScopedActivityListenerManager
import io.gitee.jesse205.activitylauncher.utils.WindowCompat

object AppThemeSupport : ScopedActivityListenerManager<AppThemeSupport.AppThemeSupportActivityListener>() {
    private const val TAG = "AppThemeSupport"
    override fun createActivityScopeListener(activity: Activity) = AppThemeSupportActivityListener()

    /**
     * 丢弃非自定义View的状态，用于解决不同主题之间的 SavedState 可能不兼容从而引发的 ClassCastException
     */
    fun discardNonViewState(savedInstanceState: Bundle) {
        savedInstanceState.getBundle(WINDOW_HIERARCHY_TAG)?.let {
            it.keySet().forEach { key ->
                if (key != WindowCompat.VIEWS_TAG) {
                    it.remove(key)
                }
            }
        }
    }

    class AppThemeSupportActivityListener : ActivityListener {
        val appTheme: AppTheme = ThemeManager.getCurrentTheme()

        override fun onActivityPreCreate(activity: Activity, savedInstanceState: Bundle?) {
            activity.apply {
                ThemeManager.applyTheme(this, appTheme)
            }
        }

        override fun onActivityResume(activity: Activity) {
            if (appTheme.id != ThemeManager.getCurrentTheme().id) {
                activity.recreate()
            }
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            if (appTheme.id != ThemeManager.getCurrentTheme().id) {
                Log.i(TAG, "onActivitySaveInstanceState: Discarded state of non-custom Views")
                discardNonViewState(outState)
            }
        }
    }
}