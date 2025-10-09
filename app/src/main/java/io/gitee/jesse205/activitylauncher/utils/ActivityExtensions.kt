package io.gitee.jesse205.activitylauncher.utils

import android.app.Activity
import android.preference.Preference
import android.preference.PreferenceActivity
import io.gitee.jesse205.activitylauncher.R


@Suppress("DEPRECATION")
inline fun <reified T : Preference> PreferenceActivity.findPreferenceCompat(key: String): T? {
    return findPreference(key) as? T?
}

object ActivityCompat {
    const val WINDOW_HIERARCHY_TAG: String = "android:viewHierarchyState"
}

val Activity.shouldApplyEdgeToEdge: Boolean
    get() = isEdgeToEdgeSupported && theme.getBoolean(R.attr.enableEdgeToEdge, false)