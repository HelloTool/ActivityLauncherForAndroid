package io.gitee.jesse205.activitylauncher.theme

import android.content.Context
import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import io.gitee.jesse205.activitylauncher.R
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppTheme(
    val group: String,
    val id: String,
    @field:StyleRes
    val style: Int,
    @field:StyleRes
    val overlayStyle: Int,
    @field:StringRes val displayNames: IntArray
) : Parcelable {
    fun getDisplayName(context: Context): String {
        val separator = context.getString(R.string.settings_theme_separator)
        return displayNames.joinToString(separator) { context.getString(it) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AppTheme

        if (style != other.style) return false
        if (overlayStyle != other.overlayStyle) return false
        if (id != other.id) return false
        if (!displayNames.contentEquals(other.displayNames)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = style
        result = 31 * result + overlayStyle
        result = 31 * result + id.hashCode()
        result = 31 * result + displayNames.contentHashCode()
        return result
    }
}
