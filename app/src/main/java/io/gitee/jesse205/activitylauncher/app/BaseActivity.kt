package io.gitee.jesse205.activitylauncher.app

import android.app.Activity
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import io.gitee.jesse205.activitylauncher.R
import io.gitee.jesse205.activitylauncher.utils.ActivityListener
import io.gitee.jesse205.activitylauncher.utils.Listenable
import io.gitee.jesse205.activitylauncher.utils.getParcelableCompat
import io.gitee.jesse205.activitylauncher.utils.isDeviceSettingsThemeSupported
import io.gitee.jesse205.activitylauncher.utils.isDeviceThemeSupported
import io.gitee.jesse205.activitylauncher.utils.isHoloThemeSupported
import io.gitee.jesse205.activitylauncher.utils.patches.EasyGoPatch
import io.gitee.jesse205.activitylauncher.utils.setDecorFitsSystemWindowsCompat


abstract class BaseActivity<S : BaseActivityState<*>> : Activity(), Listenable<ActivityListener> {
    protected abstract val stateClass: Class<S>
    private var _state: S? = null
    protected val state: S get() = _state!!
    private var listeners: MutableList<ActivityListener> = mutableListOf()
    protected open val enableEdgeToEdge = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /* if (isEmui) {
            theme.applyStyle(R.style.ThemeOverlay_ActivityLauncher_Emui, true)
        } */
        when {
            isDeviceSettingsThemeSupported -> {
                setTheme(android.R.style.Theme_DeviceDefault_Settings)
                theme.applyStyle(R.style.ThemeOverlay_ActivityLauncher_DeviceDefault_Settings, true)
            }

            isDeviceThemeSupported -> {
                setTheme(android.R.style.Theme_DeviceDefault_Light)
                theme.applyStyle(R.style.ThemeOverlay_ActivityLauncher_DeviceDefault_Light, true)
            }

            isHoloThemeSupported -> {
                @Suppress("DEPRECATION")
                setTheme(android.R.style.Theme_Holo_Light)
                theme.applyStyle(R.style.ThemeOverlay_ActivityLauncher_Holo_Light, true)
            }

            else -> {
                setTheme(android.R.style.Theme_Light)
                theme.applyStyle(R.style.ThemeOverlay_ActivityLauncher_Gingerbread_Light, true)
            }
        }


        _state =
            stateClass.cast(lastNonConfigurationInstance)
                ?: savedInstanceState?.getParcelableCompat(KEY_ACTIVITY_STATE, stateClass)
                        ?: onCreateState()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            addListener(EasyGoPatch())
        }

        if (enableEdgeToEdge) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                // 安卓 10 才引入手势导航，之前的版本没必要启用
                window.apply {
                    setDecorFitsSystemWindowsCompat(false)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        isNavigationBarContrastEnforced = true
                        @Suppress("DEPRECATION")
                        navigationBarColor = Color.TRANSPARENT
                    }
                }
            }
        }

        listeners.forEach { it.onActivityCreate(this, savedInstanceState) }
    }

    override fun onRetainNonConfigurationInstance() = state

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(KEY_ACTIVITY_STATE, state)
    }

    abstract fun onCreateState(): S?

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

    companion object {
        private const val KEY_ACTIVITY_STATE = "activity_state"
    }
}