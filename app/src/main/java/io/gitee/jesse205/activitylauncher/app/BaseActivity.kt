package io.gitee.jesse205.activitylauncher.app

import android.app.Activity
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import io.gitee.jesse205.activitylauncher.theme.AppThemeSupport
import io.gitee.jesse205.activitylauncher.utils.ActivityListener
import io.gitee.jesse205.activitylauncher.utils.Listenable
import io.gitee.jesse205.activitylauncher.utils.enableEdgeToEdge
import io.gitee.jesse205.activitylauncher.utils.getParcelableCompat
import io.gitee.jesse205.activitylauncher.utils.isNavigationGestureSupported
import io.gitee.jesse205.activitylauncher.utils.patches.EasyGoPatch
import io.gitee.jesse205.activitylauncher.utils.patches.SystemBarAppearancePatch


abstract class BaseActivity<S : BaseActivityState<*>> : Activity(), Listenable<ActivityListener> {
    protected abstract val stateClass: Class<S>
    private var _state: S? = null
    protected val state: S get() = _state!!
    private var listeners: MutableList<ActivityListener> = mutableListOf()

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
        _state =
            stateClass.cast(lastNonConfigurationInstance)
                ?: savedInstanceState?.getParcelableCompat(KEY_ACTIVITY_STATE, stateClass)
                        ?: onCreateState()


        if (isNavigationGestureSupported) {
            enableEdgeToEdge()
        }

        listeners.forEach { it.onActivityCreate(this, savedInstanceState) }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        listeners.forEach { it.onActivityPostCreate(this, savedInstanceState) }
    }

    override fun onRetainNonConfigurationInstance() = state

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        listeners.forEach { it.onActivitySaveInstanceState(this, outState) }
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