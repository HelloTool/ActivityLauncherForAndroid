package io.gitee.jesse205.activitylauncher.util

interface Listenable<Listener> {
    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}