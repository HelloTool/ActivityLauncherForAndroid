package io.gitee.jesse205.activitylauncher.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.os.Build
import android.os.FileUriExposedException
import androidx.annotation.StringRes
import io.gitee.jesse205.activitylauncher.R

val Throwable.isPermissionDenial: Boolean
    get() = this is SecurityException && message?.lowercase()?.contains("permission denial") == true


@get:StringRes
val Throwable.messageResId: Int?
    get() = when {
        this is ActivityNotFoundException -> R.string.error_no_activity_found
        isPermissionDenial -> R.string.error_permission_denied
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && this is FileUriExposedException -> R.string.error_file_uri_not_allowed
        else -> null
    }

fun Throwable.getUserFriendlyMessage(context: Context): String {
    return messageResId?.let { context.getString(it) }
        ?: localizedMessage
        ?: message
        ?: context.getString(R.string.error_unknown)
}
