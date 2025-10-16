package io.gitee.jesse205.activitylauncher.theme

import android.content.Context
import androidx.annotation.StringRes
import io.gitee.jesse205.activitylauncher.R

interface AppTheme {
    val id: String

    @get:StringRes
    val displayNames: List<Int>

    fun getDisplayName(context: Context): String {
        val separator = context.getString(R.string.settings_theme_separator)
        return displayNames.joinToString(separator) { context.getString(it) }
    }
}