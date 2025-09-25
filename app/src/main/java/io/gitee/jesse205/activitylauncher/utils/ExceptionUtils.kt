package io.gitee.jesse205.activitylauncher.utils

import java.util.Locale

fun isPermissionDenialException(e: Throwable?): Boolean {
    return if (e is SecurityException) {
        e.message != null && e.message!!.lowercase(Locale.getDefault()).contains("permission denial")
    } else {
        false
    }
}