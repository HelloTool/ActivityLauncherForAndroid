package io.gitee.jesse205.activitylauncher.model

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

data class LoadedAppInfo(
    val applicationInfo: ApplicationInfo,
    val packageInfo: PackageInfo,
    var label: CharSequence? = null,
    val packageName: String = applicationInfo.packageName
) {
    fun loadLabel(packageManager: PackageManager): CharSequence {
        return label ?: applicationInfo.loadLabel(packageManager).also {
            label = it
        }
    }

    fun loadIcon(packageManager: PackageManager): Drawable? {
        return applicationInfo.loadIcon(packageManager)
    }
}


