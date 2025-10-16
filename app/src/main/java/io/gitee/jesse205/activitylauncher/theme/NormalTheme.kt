package io.gitee.jesse205.activitylauncher.theme

import androidx.annotation.StringRes
import androidx.annotation.StyleRes

data class NormalTheme(
    override val id: String,
    @field:StringRes
    override val displayNames: List<Int>,
    @field:StyleRes
    override val style: Int,
    @field:StyleRes
    override val overlayStyle: Int
) : AppThemeVariant, AppTheme