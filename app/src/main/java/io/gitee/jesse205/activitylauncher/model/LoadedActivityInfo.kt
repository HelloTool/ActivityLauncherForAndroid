package io.gitee.jesse205.activitylauncher.model

import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

class LoadedActivityInfo(
    val activityInfo: ActivityInfo,
) {
    val name: String get() = activityInfo.name
    val exported get() = activityInfo.exported

    var label: CharSequence? = null
        private set

    val isLabelLoaded get() = label != null


    fun loadLabel(packageManager: PackageManager): CharSequence {
        return label ?: activityInfo.loadLabel(packageManager).also {
            label = it
        }
    }

    fun loadIcon(packageManager: PackageManager): Drawable? {
        return activityInfo.loadIcon(packageManager)
    }
}