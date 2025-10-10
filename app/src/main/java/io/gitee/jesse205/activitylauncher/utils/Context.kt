package io.gitee.jesse205.activitylauncher.utils

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.os.Build
import android.os.FileUriExposedException
import android.text.ClipboardManager
import android.widget.Toast
import androidx.annotation.StringRes
import io.gitee.jesse205.activitylauncher.R

@Suppress("DEPRECATION")
val Context.clipboard get() = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

fun Context.copyText(label: CharSequence?, text: CharSequence?) {
    val clipboard = clipboard
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && clipboard is android.content.ClipboardManager) {
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    } else {
        @Suppress("DEPRECATION")
        clipboard.text = text
    }
}

fun Context.showToast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, duration).show()
}

fun Context.showToast(@StringRes text: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, duration).show()
}

fun Context.showToastForThrowable(throwable: Throwable) {
    if (throwable is ActivityNotFoundException) {
        showToast(R.string.error_no_activity_found)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && throwable is FileUriExposedException) {
        showToast(R.string.error_file_uri_not_allowed)
    }
}