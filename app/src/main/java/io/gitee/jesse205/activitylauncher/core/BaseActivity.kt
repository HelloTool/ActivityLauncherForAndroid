package io.gitee.jesse205.activitylauncher.core

import android.app.Activity
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import io.gitee.jesse205.activitylauncher.utils.ActivityListener
import io.gitee.jesse205.activitylauncher.utils.EasyGoPatch
import io.gitee.jesse205.activitylauncher.utils.getParcelableCompat


abstract class BaseActivity<S : Parcelable> : Activity() {
    protected abstract val stateClass: Class<S>
    protected lateinit var state: S
    protected var isPaused = true
    protected var isPendingRecreate = false

    protected var easyGoPatch: EasyGoPatch? = null
    private var activityListeners: MutableList<ActivityListener> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        state =
            stateClass.cast(lastNonConfigurationInstance)
                ?: savedInstanceState?.getParcelableCompat(KEY_ACTIVITY_STATE, stateClass)
                        ?: onCreateState()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            addActivityListener(EasyGoPatch(this))
        }
        activityListeners.forEach { it.onCreate(savedInstanceState) }
    }

    override fun onRetainNonConfigurationInstance() = state

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(KEY_ACTIVITY_STATE, state);
    }

    abstract fun onCreateState(): S

    override fun onMultiWindowModeChanged(isInMultiWindowMode: Boolean, newConfig: Configuration) {
        super.onMultiWindowModeChanged(isInMultiWindowMode, newConfig)
        activityListeners.forEach { it.onMultiWindowModeChanged(isInMultiWindowMode, newConfig) }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        activityListeners.forEach { it.onConfigurationChanged(newConfig) }
    }

    override fun onResume() {
        super.onResume()
        activityListeners.forEach { it.onResume() }
    }

    override fun onPause() {
        super.onPause()
        activityListeners.forEach { it.onPause() }
    }

    fun addActivityListener(activityListener: ActivityListener) {
        activityListeners.add(activityListener)
    }

    companion object {
        private const val KEY_ACTIVITY_STATE = "activity_state"

    }
}