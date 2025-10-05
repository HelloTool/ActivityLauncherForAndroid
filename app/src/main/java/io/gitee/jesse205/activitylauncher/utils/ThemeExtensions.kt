package io.gitee.jesse205.activitylauncher.utils

import android.content.res.Resources
import androidx.annotation.AttrRes

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