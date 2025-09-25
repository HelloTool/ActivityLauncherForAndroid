package io.gitee.jesse205.activitylauncher.utils

import android.view.View
import android.widget.TextView

fun TextView.setTextOrGone(text: CharSequence?) {
    if (text.isNullOrBlank()) {
        visibility = View.GONE
        this.text = null
    } else {
        visibility = View.VISIBLE
        this.text = text
    }
}