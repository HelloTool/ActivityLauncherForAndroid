package io.gitee.jesse205.activitylauncher.features.applist

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

class AppItem(
    val applicationInfo: ApplicationInfo,
    val firstInstallTime: Long,
    val lastUpdateTime: Long
) {
    val packageName: String get() = applicationInfo.packageName

    @Volatile
    var label: CharSequence? = null
        private set

    val isLabelLoaded get() = label != null

    val labelLock = Any()

    fun getOrLoadLabel(packageManager: PackageManager): CharSequence {
        return label ?: synchronized(labelLock) {
            label ?: loadLabel(packageManager)
        }
    }

    fun loadLabel(packageManager: PackageManager): CharSequence {
        return applicationInfo.loadLabel(packageManager).also {
            label = it
        }
    }

    fun loadIcon(packageManager: PackageManager): Drawable? {
        return applicationInfo.loadIcon(packageManager)
    }
}