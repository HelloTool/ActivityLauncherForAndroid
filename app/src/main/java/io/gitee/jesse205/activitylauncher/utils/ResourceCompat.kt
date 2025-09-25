package io.gitee.jesse205.activitylauncher.utils

import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Build

fun Resources.getDrawableCompat(resId: Int, theme: Resources.Theme? = null): Drawable? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        getDrawable(resId, theme)
    } else {
        @Suppress("DEPRECATION")
        getDrawable(resId)
    }
}