package io.gitee.jesse205.activitylauncher.theme

import androidx.annotation.StringRes

data class DayNightTheme(
    override val id: String,
    @field:StringRes
    override val displayNames: List<Int>,
    val light: AppThemeVariant,
    val lightDarkActionBar: AppThemeVariant? = null,
    val dark: AppThemeVariant,
) : AppTheme