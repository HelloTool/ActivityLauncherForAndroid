package io.gitee.jesse205.activitylauncher.utils

import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle

interface ActivityListener {
    fun onActivityPreCreate(activity: Activity, savedInstanceState: Bundle?) {}
    fun onActivityCreate(activity: Activity, savedInstanceState: Bundle?) {}
    fun onActivityPostCreate(activity: Activity, savedInstanceState: Bundle?) {}
    fun onActivityStart(activity: Activity) {}
    fun onActivityResume(activity: Activity) {}
    fun onActivityPause(activity: Activity) {}
    fun onActivityStop(activity: Activity) {}
    fun onActivityDestroy(activity: Activity) {}
    fun onActivityMultiWindowModeChanged(activity: Activity, isInMultiWindowMode: Boolean, newConfig: Configuration) {}
    fun onActivityConfigurationChanged(activity: Activity, newConfig: Configuration) {}

    fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    fun onActivityPreRestoreInstanceState(activity: Activity, savedInstanceState: Bundle) {}
    fun onActivityRestoreInstanceState(activity: Activity, savedInstanceState: Bundle) {}
}