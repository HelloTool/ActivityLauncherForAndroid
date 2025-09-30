package io.gitee.jesse205.activitylauncher.utils

import android.os.StrictMode
import android.util.Log

private const val TAG = "StrictModeUtils"

fun disableDeathOnFileUriExposure() {
    runCatching {
        with(StrictMode::class.java.getMethod("disableDeathOnFileUriExposure")) {
            isAccessible = true
            invoke(null)
        }
    }.onFailure {
        Log.w(TAG, "onCreate: Failed to disable death on file uri exposure", it)
    }
}