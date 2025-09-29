package io.gitee.jesse205.activitylauncher.core

import android.app.Activity
import android.os.Parcelable
import io.gitee.jesse205.activitylauncher.utils.ActivityListener
import io.gitee.jesse205.activitylauncher.utils.Listenable

abstract class BaseActivityState<Listener> : Parcelable, Listenable<Listener> {
    protected var listeners: MutableList<Listener> = mutableListOf()
    override fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    open fun destroy() {
        listeners.clear()
    }

    fun bind(listenableActivity: Listenable<ActivityListener>, listener: Listener) {
        this@BaseActivityState.addListener(listener)
        listenableActivity.addListener(object : ActivityListener {
            override fun onActivityDestroy(activity: Activity) {
                if (activity.isFinishing) {
                    this@BaseActivityState.destroy()
                } else {
                    this@BaseActivityState.removeListener(listener)
                }
            }
        })
    }
}