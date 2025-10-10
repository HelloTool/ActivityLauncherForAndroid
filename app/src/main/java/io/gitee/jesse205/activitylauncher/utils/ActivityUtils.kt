package io.gitee.jesse205.activitylauncher.utils

import android.preference.Preference
import android.preference.PreferenceActivity


@Suppress("DEPRECATION")
inline fun <reified T : Preference> PreferenceActivity.findPreferenceCompat(key: String): T? {
    return findPreference(key) as? T?
}

object ActivityCompat {
    const val WINDOW_HIERARCHY_TAG: String = "android:viewHierarchyState"
}
