package io.gitee.jesse205.activitylauncher.utils

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo

fun PackageInfo.isSystemApp(): Boolean {
    return applicationInfo?.flags?.and(ApplicationInfo.FLAG_SYSTEM) != 0
}

val PackageInfo.appProvisionType
    get() = if (isSystemApp()) {
        AppProvisionType.SYSTEM
    } else {
        AppProvisionType.USER
    }