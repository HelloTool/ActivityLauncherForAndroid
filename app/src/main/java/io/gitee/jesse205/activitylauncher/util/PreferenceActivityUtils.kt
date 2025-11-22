@file:Suppress("DEPRECATION")

package io.gitee.jesse205.activitylauncher.util

import android.preference.Preference
import android.preference.PreferenceActivity

inline fun <reified T : Preference> PreferenceActivity.findPreferenceCompat(key: String): T? {
    return findPreference(key) as? T?
}