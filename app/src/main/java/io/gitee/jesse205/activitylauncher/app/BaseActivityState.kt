package io.gitee.jesse205.activitylauncher.app

import android.app.Activity
import android.os.Parcelable
import io.gitee.jesse205.activitylauncher.utils.ActivityListener
import io.gitee.jesse205.activitylauncher.utils.Listenable

abstract class BaseActivityState<StateListener> : Parcelable, Listenable<StateListener> {
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