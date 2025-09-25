package io.gitee.jesse205.activitylauncher.model

import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

data class LoadedActivityInfo(
    var label: CharSequence? = null,
    val activityInfo: ActivityInfo,
    val name: String = activityInfo.name
) {
    fun loadLabel(packageManager: PackageManager): CharSequence {
        return label ?: activityInfo.loadLabel(packageManager).also {
            label = it
        }
    }

    fun loadIcon(packageManager: PackageManager): Drawable? {
        return activityInfo.loadIcon(packageManager)
    }
}