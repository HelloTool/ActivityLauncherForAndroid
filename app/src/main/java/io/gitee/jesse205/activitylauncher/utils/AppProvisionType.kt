package io.gitee.jesse205.activitylauncher.utils

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