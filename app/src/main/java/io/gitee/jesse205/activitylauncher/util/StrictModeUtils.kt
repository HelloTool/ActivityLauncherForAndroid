package io.gitee.jesse205.activitylauncher.util

import android.os.Build
import android.os.StrictMode
import android.util.Log
import androidx.annotation.RequiresApi

private const val TAG = "StrictModeUtils"

@RequiresApi(Build.VERSION_CODES.N)
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