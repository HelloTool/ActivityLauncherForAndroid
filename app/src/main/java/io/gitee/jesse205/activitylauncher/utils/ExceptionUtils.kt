package io.gitee.jesse205.activitylauncher.utils

import java.util.Locale

fun Throwable?.isPermissionDenial(): Boolean {
    return if (this is SecurityException) {
        message != null && message!!.lowercase(Locale.getDefault()).contains("permission denial")
    } else {
        false
    }
}