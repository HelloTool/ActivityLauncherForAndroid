package io.gitee.jesse205.activitylauncher.app

import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle
import io.gitee.jesse205.activitylauncher.util.ActivityListener
import io.gitee.jesse205.activitylauncher.util.Listenable
import io.gitee.jesse205.activitylauncher.util.ViewModel
import io.gitee.jesse205.activitylauncher.util.ViewModelStore


abstract class BaseActivity : Activity(), Listenable<ActivityListener>, ViewModelStore {

    private lateinit var viewModels: MutableMap<String, ViewModel<*>>

    private val helper = BaseActivityHelper(this)

    var resumed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        helper.onActivityPreCreate(savedInstanceState)
        super.onCreate(savedInstanceState)

        @Suppress("UNCHECKED_CAST")
        viewModels = lastNonConfigurationInstance as? MutableMap<String, ViewModel<*>> ?: mutableMapOf()

        helper.onActivityCreate(savedInstanceState)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        helper.onActivityPostCreate(savedInstanceState)
    }

    override fun onRetainNonConfigurationInstance() = viewModels

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModels.forEach { (key, value) ->
            outState.putBundle(VIEW_MODEL_STATE_TAG_PREFIX + key, value.saveHierarchyState())
        }
        helper.onActivitySaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        helper.onActivityPreRestoreInstanceState(savedInstanceState)
        super.onRestoreInstanceState(savedInstanceState)
        viewModels.forEach { (key, value) ->
            savedInstanceState.getBundle(VIEW_MODEL_STATE_TAG_PREFIX + key).let {
                value.restoreHierarchyState(it)
            }
        }
        helper.onActivityRestoreInstanceState(savedInstanceState)
    }

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
        if (isFinishing) {
            viewModels.values.forEach {
                it.destroy()
            }
        }
    }

    override fun addListener(listener: ActivityListener) {
        helper.addListener(listener)
    }

    override fun removeListener(listener: ActivityListener) {
        helper.removeListener(listener)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel<*>> getViewModel(clazz: Class<T>): T? = viewModels[clazz.name] as T?

    override fun <T : ViewModel<*>> setViewModel(clazz: Class<T>, value: T) {
        viewModels[clazz.name] = value
    }

    companion object {
        private const val VIEW_MODEL_STATE_TAG_PREFIX = "viewModelState_"
    }
}