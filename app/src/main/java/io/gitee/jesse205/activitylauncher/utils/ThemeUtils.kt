package io.gitee.jesse205.activitylauncher.utils

import android.content.res.Resources
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import io.gitee.jesse205.activitylauncher.R

fun Resources.Theme.getDimensionPixelSize(@AttrRes resId: Int): Int {
    return obtainStyledAttributes(intArrayOf(resId)).useCompat {
        it.getDimensionPixelSize(0, 0)
    }
}

fun Resources.Theme.getBoolean(@AttrRes resId: Int, defValue: Boolean): Boolean {
    return obtainStyledAttributes(intArrayOf(resId)).useCompat {
        it.getBoolean(0, defValue)
    }
}

fun Resources.Theme.getColor(@AttrRes resId: Int, @ColorInt defValue: Int): Int {
    return obtainStyledAttributes(intArrayOf(resId)).useCompat {
        it.getColor(0, defValue)
    }
}

val Resources.Theme.isSupportEdgeToEdge
    get() = getBoolean(R.attr.supportEdgeToEdge, false)
