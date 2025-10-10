package io.gitee.jesse205.activitylauncher.utils

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo

val PackageInfo.isSystemApp
    get() = applicationInfo?.let { it.flags.and(ApplicationInfo.FLAG_SYSTEM) != 0 } ?: false
