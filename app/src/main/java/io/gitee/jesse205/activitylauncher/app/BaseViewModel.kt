package io.gitee.jesse205.activitylauncher.app

import android.app.Activity
import android.os.Bundle
import io.gitee.jesse205.activitylauncher.util.ActivityListener
import io.gitee.jesse205.activitylauncher.util.Listenable

abstract class BaseViewModel<StateListener> : Listenable<StateListener> {
    protected var listeners: MutableList<StateListener> = mutableListOf()
    override fun addListener(listener: StateListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: StateListener) {
        listeners.remove(listener)
    }

    open fun destroy() {
        listeners.clear()
    }

    open fun saveHierarchyState(): Bundle? = null

    open fun restoreHierarchyState(state: Bundle?) {}

    open fun bind(listenableActivity: Listenable<ActivityListener>, listener: StateListener) {
        addListener(listener)
        listenableActivity.addListener(object : ActivityListener {
            override fun onActivityDestroy(activity: Activity) {
                if (activity.isFinishing) {
                    destroy()
                } else {
                    removeListener(listener)
                }
            }
        })
    }
}