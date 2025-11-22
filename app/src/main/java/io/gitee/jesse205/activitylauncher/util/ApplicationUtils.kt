package io.gitee.jesse205.activitylauncher.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.pm.ApplicationInfo


fun ApplicationInfo.isOpenable(context: Context): Boolean {
    return context.isAppOpenable(packageName)
}

fun Context.isAppOpenable(packageName: String): Boolean {
    val intent = packageManager.getLaunchIntentForPackage(packageName)
    return intent != null
}

fun Context.openApp(packageName: String) {
    val intent = packageManager.getLaunchIntentForPackage(packageName) ?: throw ActivityNotFoundException(packageName)
    startActivity(intent)
}