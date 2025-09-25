package io.gitee.jesse205.activitylauncher.utils

import android.os.Build
import android.util.Log
import android.widget.ArrayAdapter


fun <T> ArrayAdapter<T>.addAllCompat(collection: MutableCollection<out T>) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        addAll(collection)
    } else {
        for (t in collection) {
            add(t)
        }
    }
}