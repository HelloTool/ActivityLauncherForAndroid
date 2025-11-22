package io.gitee.jesse205.activitylauncher.feature.applist

import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import io.gitee.jesse205.activitylauncher.util.AppProvisionType
import io.gitee.jesse205.activitylauncher.util.AppSortCategory

class MainActivityPreferences(val context: Activity) {
    private val sharedPreferences: SharedPreferences = context.getPreferences(MODE_PRIVATE)

    var provisionType: AppProvisionType?
        get() = sharedPreferences.getString(PREFERENCE_KEY_PROVISION_TYPE, null)
            ?.let {
                runCatching {
                    AppProvisionType.valueOf(it)
                }.getOrNull()
            }
        set(value) {
            sharedPreferences.edit()
                .putString(PREFERENCE_KEY_PROVISION_TYPE, value?.name)
                .apply()
        }

    var sortCategory: AppSortCategory?
        get() = sharedPreferences.getString(PREFERENCE_KEY_SORT_CATEGORY, null)
            ?.let {
                runCatching {
                    AppSortCategory.valueOf(it)
                }.getOrNull()
            }
        set(value) {
            sharedPreferences.edit()
                .putString(PREFERENCE_KEY_SORT_CATEGORY, value?.name)
                .apply()
        }


    companion object {
        private const val PREFERENCE_KEY_PROVISION_TYPE = "provision_type"
        private const val PREFERENCE_KEY_SORT_CATEGORY = "sort_category"
    }
}