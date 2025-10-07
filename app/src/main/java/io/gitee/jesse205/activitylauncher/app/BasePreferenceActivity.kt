@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package io.gitee.jesse205.activitylauncher.app

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceActivity
import io.gitee.jesse205.activitylauncher.theme.AppThemeSupport
import io.gitee.jesse205.activitylauncher.utils.ActivityListener
import io.gitee.jesse205.activitylauncher.utils.Listenable
import io.gitee.jesse205.activitylauncher.utils.patches.EasyGoPatch
import io.gitee.jesse205.activitylauncher.utils.patches.SystemBarAppearancePatch

open class BasePreferenceActivity : PreferenceActivity(), Listenable<ActivityListener> {
    private var listeners: MutableList<ActivityListener> = mutableListOf()
    protected open val enableEdgeToEdge = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    override fun onCreate(savedInstanceState: Bundle?) {
        addListener(AppThemeSupport)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            addListener(EasyGoPatch)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            addListener(SystemBarAppearancePatch)
        }
        listeners.forEach { it.onActivityPreCreate(this, savedInstanceState) }
        super.onCreate(savedInstanceState)
        listeners.forEach { it.onActivityCreate(this, savedInstanceState) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        listeners.forEach { it.onActivitySaveInstanceState(this, outState) }
    }

    override fun onMultiWindowModeChanged(isInMultiWindowMode: Boolean, newConfig: Configuration) {
        super.onMultiWindowModeChanged(isInMultiWindowMode, newConfig)
        listeners.forEach { it.onActivityMultiWindowModeChanged(this, isInMultiWindowMode, newConfig) }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        listeners.forEach { it.onActivityConfigurationChanged(this, newConfig) }
    }

    override fun onResume() {
        super.onResume()
        listeners.forEach { it.onActivityResume(this) }
    }

    override fun onPause() {
        super.onPause()
        listeners.forEach { it.onActivityPause(this) }
    }

    override fun onDestroy() {
        super.onDestroy()
        listeners.forEach { it.onActivityDestroy(this) }
    }

    override fun addListener(listener: ActivityListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: ActivityListener) {
        listeners.remove(listener)
    }
}