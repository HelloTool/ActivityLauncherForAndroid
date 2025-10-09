package io.gitee.jesse205.activitylauncher.app

import android.app.Activity
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.Window
import io.gitee.jesse205.activitylauncher.theme.AppThemeSupport
import io.gitee.jesse205.activitylauncher.utils.ActivityListener
import io.gitee.jesse205.activitylauncher.utils.Listenable
import io.gitee.jesse205.activitylauncher.utils.isNavigationGestureSupported
import io.gitee.jesse205.activitylauncher.utils.isSupportEdgeToEdge
import io.gitee.jesse205.activitylauncher.utils.patches.ScreenSizeChangePatch
import io.gitee.jesse205.activitylauncher.utils.patches.SystemBarAppearancePatch
import io.gitee.jesse205.activitylauncher.utils.setDecorFitsSystemWindowsCompat

class BaseActivityHelper(val activity: Activity) : Listenable<ActivityListener> {
    private val listeners: MutableList<ActivityListener> = mutableListOf<ActivityListener>().apply {
        add(AppThemeSupport)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            add(ScreenSizeChangePatch)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            add(SystemBarAppearancePatch)
        }
    }

    fun onActivityPreCreate(savedInstanceState: Bundle?) {
        listeners.forEach { it.onActivityPreCreate(activity, savedInstanceState) }
        if (isNavigationGestureSupported) {
            activity.requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY)
        }
    }

    fun onActivityCreate(savedInstanceState: Bundle?) {
        listeners.forEach { it.onActivityCreate(activity, savedInstanceState) }

    }

    fun onActivityPostCreate(savedInstanceState: Bundle?) {
        listeners.forEach { it.onActivityPostCreate(activity, savedInstanceState) }
        if (isNavigationGestureSupported) {
            activity.window.setDecorFitsSystemWindowsCompat(!activity.theme.isSupportEdgeToEdge)
        }
    }

    fun onActivityResume() {
        listeners.forEach { it.onActivityResume(activity) }
    }

    fun onActivityPause() {
        listeners.forEach { it.onActivityPause(activity) }
    }

    fun onActivityDestroy() {
        listeners.forEach { it.onActivityDestroy(activity) }
    }

    fun onActivityMultiWindowModeChanged(isInMultiWindowMode: Boolean, newConfig: Configuration) {
        listeners.forEach { it.onActivityMultiWindowModeChanged(activity, isInMultiWindowMode, newConfig) }
    }

    fun onActivityConfigurationChanged(newConfig: Configuration) {
        listeners.forEach { it.onActivityConfigurationChanged(activity, newConfig) }
    }

    fun onActivitySaveInstanceState(outState: Bundle) {
        listeners.forEach { it.onActivitySaveInstanceState(activity, outState) }
    }

    override fun addListener(listener: ActivityListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: ActivityListener) {
        listeners.remove(listener)
    }

}