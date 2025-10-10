package io.gitee.jesse205.activitylauncher.utils

import android.content.ClipData
import android.content.Context
import android.os.Build
import android.text.ClipboardManager
import android.widget.Toast
import androidx.annotation.StringRes

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