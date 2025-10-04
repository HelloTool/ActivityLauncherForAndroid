package io.gitee.jesse205.activitylauncher.utils

import android.content.res.Resources
import androidx.annotation.AttrRes


fun Resources.Theme.getDimension(@AttrRes resId: Int): Float {
    return obtainStyledAttributes(intArrayOf(resId)).useCompat {
        it.getDimension(0, 0F)
    }
}

fun Resources.Theme.getDimensionPixelSize(@AttrRes resId: Int): Int {
    return obtainStyledAttributes(intArrayOf(resId)).useCompat {
        it.getDimensionPixelSize(0, 0)
    }
}