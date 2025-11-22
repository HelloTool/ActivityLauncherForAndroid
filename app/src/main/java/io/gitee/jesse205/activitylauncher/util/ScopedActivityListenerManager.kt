package io.gitee.jesse205.activitylauncher.util

import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle
import java.util.WeakHashMap

/**
 * 一个有状态的 Activity 监听器抽象类，为每个 Activity 实例创建并管理独立的监听器实例。
 *
 * 该类的主要功能包括:
 *
 * 1. 为每个 Activity 创建独立的作用域监听器实例
 * 2. 管理这些监听器实例的生命周期
 * 3. 将全局 Activity 事件分发给对应的监听器实例
 *
 * 使用 WeakHashMap 存储 Activity 和监听器的映射关系，避免内存泄漏。
 * 当 Activity 销毁时，自动清理对应的监听器实例。
 *
 * @param T 继承自 ActivityListener 的具体监听器类型
 */
abstract class ScopedActivityListenerManager<T : ActivityListener> : ActivityListener {
    private val listenerMap: MutableMap<Activity, T> = WeakHashMap()

    /**
     * 创建与特定 Activity 关联的监听器实例
     *
     * @param activity 关联的 Activity 实例
     * @return 与该 Activity 关联的监听器实例
     */
    protected abstract fun createActivityScopeListener(activity: Activity): T

    fun getActivityScopeListener(activity: Activity): T =
        listenerMap[activity] ?: createActivityScopeListener(activity).also {
            listenerMap[activity] = it
        }

    final override fun onActivityPreCreate(activity: Activity, savedInstanceState: Bundle?) {
        getActivityScopeListener(activity).onActivityPreCreate(activity, savedInstanceState)

    }

    final override fun onActivityCreate(activity: Activity, savedInstanceState: Bundle?) {
        getActivityScopeListener(activity).onActivityCreate(activity, savedInstanceState)
    }

    final override fun onActivityPostCreate(activity: Activity, savedInstanceState: Bundle?) {
        getActivityScopeListener(activity).onActivityPostCreate(activity, savedInstanceState)
    }

    final override fun onActivityStart(activity: Activity) {
        getActivityScopeListener(activity).onActivityStart(activity)
    }

    final override fun onActivityResume(activity: Activity) {
        getActivityScopeListener(activity).onActivityResume(activity)
    }

    final override fun onActivityPause(activity: Activity) {
        getActivityScopeListener(activity).onActivityPause(activity)
    }

    final override fun onActivityStop(activity: Activity) {
        getActivityScopeListener(activity).onActivityStop(activity)
    }

    final override fun onActivityDestroy(activity: Activity) {
        getActivityScopeListener(activity).onActivityDestroy(activity)
        listenerMap.remove(activity)
    }

    final override fun onActivityMultiWindowModeChanged(
        activity: Activity,
        isInMultiWindowMode: Boolean,
        newConfig: Configuration
    ) {
        getActivityScopeListener(activity).onActivityMultiWindowModeChanged(activity, isInMultiWindowMode, newConfig)
    }

    final override fun onActivityConfigurationChanged(activity: Activity, newConfig: Configuration) {
        getActivityScopeListener(activity).onActivityConfigurationChanged(activity, newConfig)
    }

    final override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        getActivityScopeListener(activity).onActivitySaveInstanceState(activity, outState)
    }

    final override fun onActivityPreRestoreInstanceState(activity: Activity, savedInstanceState: Bundle) {
        getActivityScopeListener(activity).onActivityPreRestoreInstanceState(activity, savedInstanceState)
    }

    final override fun onActivityRestoreInstanceState(activity: Activity, savedInstanceState: Bundle) {
        getActivityScopeListener(activity).onActivityRestoreInstanceState(activity, savedInstanceState)
    }
}