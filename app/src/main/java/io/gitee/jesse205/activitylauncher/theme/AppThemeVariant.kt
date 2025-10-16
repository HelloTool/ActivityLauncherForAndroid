package io.gitee.jesse205.activitylauncher.theme

import androidx.annotation.StyleRes

/**
 * 主题变体，代表深色、亮色等
 */
interface AppThemeVariant {
    val id: String

    @get:StyleRes
    val style: Int

    @get:StyleRes
    val overlayStyle: Int
}