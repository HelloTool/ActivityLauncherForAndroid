package io.gitee.jesse205.activitylauncher.utils

import android.os.Build
import android.os.Bundle
import android.os.Parcelable


fun <T : Parcelable> Bundle.getParcelableCompat(key: String, clazz: Class<T>): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelable(key, clazz)
    } else {
        @Suppress("DEPRECATION")
        clazz.cast(getParcelable(key))
    }
}
