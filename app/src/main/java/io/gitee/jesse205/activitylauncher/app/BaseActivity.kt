package io.gitee.jesse205.activitylauncher.app

import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle
import io.gitee.jesse205.activitylauncher.utils.ActivityListener
import io.gitee.jesse205.activitylauncher.utils.Listenable
import io.gitee.jesse205.activitylauncher.utils.getParcelableCompat


abstract class BaseActivity<S : BaseActivityState<*>> : Activity(), Listenable<ActivityListener> {
    protected abstract val stateClass: Class<S>
    private var _state: S? = null
    protected val state: S get() = _state!!
    private val helper = BaseActivityHelper(this)


    var resumed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        helper.onActivityPreCreate(savedInstanceState)
        super.onCreate(savedInstanceState)
        _state =
            stateClass.cast(lastNonConfigurationInstance)
                ?: savedInstanceState?.getParcelableCompat(KEY_ACTIVITY_STATE, stateClass)
                        ?: onCreateState()

        helper.onActivityCreate(savedInstanceState)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        helper.onActivityPostCreate(savedInstanceState)
    }

    override fun onRetainNonConfigurationInstance() = state

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        helper.onActivitySaveInstanceState(outState)
        outState.putParcelable(KEY_ACTIVITY_STATE, state)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        helper.onActivityPreRestoreInstanceState(savedInstanceState)
        super.onRestoreInstanceState(savedInstanceState)
        helper.onActivityRestoreInstanceState(savedInstanceState)
    }

    abstract fun onCreateState(): S?

    override fun onMultiWindowModeChanged(isInMultiWindowMode: Boolean, newConfig: Configuration) {
        super.onMultiWindowModeChanged(isInMultiWindowMode, newConfig)
        helper.onActivityMultiWindowModeChanged(isInMultiWindowMode, newConfig)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        helper.onActivityConfigurationChanged(newConfig)
    }

    override fun onStart() {
        super.onStart()
        helper.onActivityStart()
    }

    override fun onResume() {
        super.onResume()
        helper.onActivityResume()
        resumed = true
    }

    override fun onPause() {
        super.onPause()
        helper.onActivityPause()
    }

    override fun onStop() {
        super.onStop()
        helper.onActivityStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        helper.onActivityDestroy()
    }

    override fun addListener(listener: ActivityListener) {
        helper.addListener(listener)
    }

    override fun removeListener(listener: ActivityListener) {
        helper.removeListener(listener)
    }

    companion object {
        private const val KEY_ACTIVITY_STATE = "activity_state"
    }
}