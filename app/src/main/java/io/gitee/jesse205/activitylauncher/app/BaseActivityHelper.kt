package io.gitee.jesse205.activitylauncher.app

import android.app.Activity
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Window
import io.gitee.jesse205.activitylauncher.BuildConfig
import io.gitee.jesse205.activitylauncher.theme.ThemeSupport
import io.gitee.jesse205.activitylauncher.utils.ActivityListener
import io.gitee.jesse205.activitylauncher.utils.Listenable
import io.gitee.jesse205.activitylauncher.utils.isNavigationGestureSupported
import io.gitee.jesse205.activitylauncher.utils.isSupportEdgeToEdge
import io.gitee.jesse205.activitylauncher.utils.patches.ScreenSizeChangePatch
import io.gitee.jesse205.activitylauncher.utils.patches.SystemBarAppearancePatch
import io.gitee.jesse205.activitylauncher.utils.setDecorFitsSystemWindowsCompat

class BaseActivityHelper(val activity: Activity) : Listenable<ActivityListener> {
    private val listeners: MutableList<ActivityListener> = mutableListOf<ActivityListener>().apply {
        add(ThemeSupport)
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
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onActivityCreate: $activity")
        }
    }

    fun onActivityPostCreate(savedInstanceState: Bundle?) {
        listeners.forEach { it.onActivityPostCreate(activity, savedInstanceState) }
        if (isNavigationGestureSupported) {
            activity.window.setDecorFitsSystemWindowsCompat(!activity.theme.isSupportEdgeToEdge)
        }
    }

    fun onActivityStart() {
        listeners.forEach { it.onActivityStart(activity) }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onActivityStart: $activity")
        }
    }

    fun onActivityResume() {
        listeners.forEach { it.onActivityResume(activity) }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onActivityResume: $activity")
        }
    }

    fun onActivityPause() {
        listeners.forEach { it.onActivityPause(activity) }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onActivityPause: $activity")
        }
    }

    fun onActivityStop() {
        listeners.forEach { it.onActivityStop(activity) }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onActivityStop: $activity")
        }
    }

    fun onActivityDestroy() {
        listeners.forEach { it.onActivityDestroy(activity) }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onActivityDestroy: $activity")
        }
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

    fun onActivityPreRestoreInstanceState(savedInstanceState: Bundle) {
        listeners.forEach { it.onActivityPreRestoreInstanceState(activity, savedInstanceState) }
    }

    fun onActivityRestoreInstanceState(savedInstanceState: Bundle) {
        listeners.forEach { it.onActivityRestoreInstanceState(activity, savedInstanceState) }
    }

    override fun addListener(listener: ActivityListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: ActivityListener) {
        listeners.remove(listener)
    }

    companion object {
        private const val TAG = "BaseActivityHelper"
    }
}