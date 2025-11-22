package io.gitee.jesse205.activitylauncher.util

import android.content.pm.PackageInfo

enum class AppProvisionType {
    USER, SYSTEM
}


val PackageInfo.appProvisionType
    get() = if (isSystemApp) {
        AppProvisionType.SYSTEM
    } else {
        AppProvisionType.USER
    }