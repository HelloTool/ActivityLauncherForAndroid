package io.gitee.jesse205.activitylauncher.utils

import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle

interface ActivityListener {
    fun onActivityCreate(activity: Activity, savedInstanceState: Bundle?) {}
    fun onActivityResume(activity: Activity) {}
    fun onActivityPause(activity: Activity) {}
    fun onActivityDestroy(activity: Activity) {}
    fun onActivityMultiWindowModeChanged(activity: Activity, isInMultiWindowMode: Boolean, newConfig: Configuration) {}
    fun onActivityConfigurationChanged(activity: Activity, newConfig: Configuration) {}
}