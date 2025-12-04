package io.gitee.jesse205.activitylauncher.app

import android.os.Bundle
import io.gitee.jesse205.activitylauncher.util.Listenable
import io.gitee.jesse205.activitylauncher.util.ViewModel

abstract class BaseViewModel<StateListener> : Listenable<StateListener>, ViewModel<StateListener> {
    protected var listeners: MutableList<StateListener> = mutableListOf()
    override fun addListener(listener: StateListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: StateListener) {
        listeners.remove(listener)
    }

    override fun destroy() {
        listeners.clear()
    }

    override fun saveHierarchyState(): Bundle? = null

    override fun restoreHierarchyState(state: Bundle?) {}

}