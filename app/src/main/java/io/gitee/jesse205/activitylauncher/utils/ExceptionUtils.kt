package io.gitee.jesse205.activitylauncher.utils

import android.content.ActivityNotFoundException
import android.os.Build
import android.os.FileUriExposedException
import androidx.annotation.StringRes
import io.gitee.jesse205.activitylauncher.R
import java.util.Locale

fun Throwable?.isPermissionDenial(): Boolean {
    return if (this is SecurityException) {
        message != null && message!!.lowercase(Locale.getDefault()).contains("permission denial")
    } else {
        false
    }
}

@get:StringRes
val Throwable.errorMessageResId: Int?
    get() = when {
        this is ActivityNotFoundException -> R.string.error_no_activity_found
        this.isPermissionDenial() -> R.string.error_permission_denied
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && this is FileUriExposedException -> R.string.error_file_uri_not_allowed
        else -> null
    }