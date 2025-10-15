package io.gitee.jesse205.activitylauncher.utils

import io.gitee.jesse205.activitylauncher.app.ActivityLauncherApp

@Suppress("ClassName", "DiscouragedApi")
object AndroidInternalR {
    object id {
        @JvmStatic()
        @get:JvmName("ACTION_BAR_ID")
        val ACTION_BAR by lazy {
            ActivityLauncherApp.INSTANCE.resources.getIdentifier("action_bar", "id", "android")
        }
        val BUTTON_PANEL by lazy {
            ActivityLauncherApp.INSTANCE.resources.getIdentifier("buttonPanel", "id", "android")
        }
    }
}