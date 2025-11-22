package io.gitee.jesse205.activitylauncher.util

import io.gitee.jesse205.activitylauncher.app.ActivityLauncherApp

@Suppress("ClassName", "DiscouragedApi")
object AndroidInternalR {
    object id {
        @JvmStatic()
        @get:JvmName("ACTION_BAR_ID")
        val action_bar by lazy {
            ActivityLauncherApp.INSTANCE.resources.getIdentifier("action_bar", "id", "android")
        }
        val buttonPanel by lazy {
            ActivityLauncherApp.INSTANCE.resources.getIdentifier("buttonPanel", "id", "android")
        }
    }
}