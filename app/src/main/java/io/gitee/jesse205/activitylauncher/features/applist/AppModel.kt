package io.gitee.jesse205.activitylauncher.features.applist

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

class AppModel(
    val applicationInfo: ApplicationInfo,
    val firstInstallTime: Long,
    val lastUpdateTime: Long
) {
    val packageName: String get() = applicationInfo.packageName

    var label: CharSequence? = null
        private set
    val isLabelLoaded get() = label != null

    fun loadLabel(packageManager: PackageManager): CharSequence {
        return label ?: applicationInfo.loadLabel(packageManager).also {
            label = it
        }
    }

    fun loadIcon(packageManager: PackageManager): Drawable? {
        return applicationInfo.loadIcon(packageManager)
    }
}