package io.gitee.jesse205.activitylauncher.utils

import android.content.ClipData
import android.content.Context
import android.os.Build
import android.text.ClipboardManager

@Suppress("DEPRECATION")
val Context.clipboard get() = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

fun Context.copyText(label: CharSequence?, text: CharSequence?) {
    val clipboard = clipboard
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && clipboard is android.content.ClipboardManager) {
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text));
    } else {
        @Suppress("DEPRECATION")
        clipboard.text = text
    }
}