package io.gitee.jesse205.activitylauncher.theme

import android.app.Activity
import android.os.Bundle
import io.gitee.jesse205.activitylauncher.utils.ActivityCompat.WINDOW_HIERARCHY_TAG
import io.gitee.jesse205.activitylauncher.utils.ActivityListener
import io.gitee.jesse205.activitylauncher.utils.WindowCompat
import io.gitee.jesse205.activitylauncher.utils.getBoolean
import io.gitee.jesse205.activitylauncher.utils.isLightNavigationBarResSupported
import io.gitee.jesse205.activitylauncher.utils.isLightStatusBarSupported
import io.gitee.jesse205.activitylauncher.utils.setSystemBarsAppearance
import java.util.WeakHashMap

object AppThemeSupport : ActivityListener {
    private const val KEY_APP_THEME = "app_theme"

    private val stateMap: MutableMap<Activity, ThemeState> = WeakHashMap()
    override fun onPostActivityCreate(activity: Activity, savedInstanceState: Bundle?) {
        val state = ThemeState(appTheme = ThemeManager.getCurrentTheme()).also {
            stateMap[activity] = it
        }

        activity.apply {
            ThemeManager.applyTheme(this, state.appTheme)
        }
    }

    override fun onActivityCreate(activity: Activity, savedInstanceState: Bundle?) {
        activity.apply {
            setSystemBarsAppearance(
                isLightSystemBars = if (isLightStatusBarSupported) {
                    theme.getBoolean(android.R.attr.windowLightStatusBar, false)
                } else {
                    false
                },
                isLightNavigationBar = if (isLightNavigationBarResSupported) {
                    theme.getBoolean(android.R.attr.windowLightNavigationBar, false)
                } else {
                    false
                }
            )
        }
    }

    override fun onActivityResume(activity: Activity) {
        val state = stateMap[activity] ?: return
        if (state.appTheme.id != ThemeManager.getCurrentTheme().id) {
            activity.recreate()
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        val state = stateMap[activity] ?: return
        if (state.appTheme.id != ThemeManager.getCurrentTheme().id) {
            discardNonViewState(outState)
        }
    }

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

    data class ThemeState(var appTheme: AppTheme)

}