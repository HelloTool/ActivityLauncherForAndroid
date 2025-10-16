package io.gitee.jesse205.activitylauncher.theme

import androidx.annotation.StyleRes

data class NormalThemeVariant(
    override val id: String,
    @field:StyleRes
    override val style: Int,
    @field:StyleRes
    override val overlayStyle: Int
) : AppThemeVariant