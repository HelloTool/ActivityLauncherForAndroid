package io.gitee.jesse205.activitylauncher.utils

import android.content.res.Configuration
import android.os.Bundle

interface ActivityListener {
    fun onCreate(savedInstanceState: Bundle?) {}
    fun onResume() {}
    fun onPause() {}
    fun onDestroy() {}
    fun onMultiWindowModeChanged(isInMultiWindowMode: Boolean, newConfig: Configuration) {}

    fun onConfigurationChanged(newConfig: Configuration) {}
}