package io.gitee.jesse205.activitylauncher.utils

import android.content.res.Resources
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import io.gitee.jesse205.activitylauncher.R

fun Resources.Theme.getDimensionPixelSize(@AttrRes attrId: Int, @StyleRes styleId: Int = 0): Int {
    return obtainStyledAttributes(styleId, intArrayOf(attrId)).useCompat {
        it.getDimensionPixelSize(0, 0)
    }
}

fun Resources.Theme.getBoolean(@AttrRes attrId: Int, defValue: Boolean, @StyleRes styleId: Int = 0): Boolean {
    return obtainStyledAttributes(styleId, intArrayOf(attrId)).useCompat {
        it.getBoolean(0, defValue)
    }
}

fun Resources.Theme.getColor(@AttrRes attrId: Int, @ColorInt defValue: Int, @StyleRes styleId: Int = 0): Int {
    return obtainStyledAttributes(styleId, intArrayOf(attrId)).useCompat {
        it.getColor(0, defValue)
    }
}

val Resources.Theme.isSupportEdgeToEdge
    get() = getBoolean(R.attr.supportEdgeToEdge, false)
