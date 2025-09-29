package io.gitee.jesse205.activitylauncher.utils

interface Listenable<Listener> {
    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}