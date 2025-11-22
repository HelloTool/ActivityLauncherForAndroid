package io.gitee.jesse205.activitylauncher.util.patch

import android.app.Activity
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import io.gitee.jesse205.activitylauncher.util.ActivityListener
import io.gitee.jesse205.activitylauncher.util.getBoolean
import io.gitee.jesse205.activitylauncher.util.isLightNavigationBarResSupported
import io.gitee.jesse205.activitylauncher.util.isLightStatusBarSupported
import io.gitee.jesse205.activitylauncher.util.setSystemBarsAppearance

/**
 * 系统栏外观补丁
 *
 * 在 Android 8.0 (API 26) 及以上版本中，当 Activity 重建时（recreate），系统栏的外观设置
 * （如状态栏和导航栏的图标颜色）可能不会被正确保留。这个补丁用于手动重新设置系统栏的外观，
 * 确保它们与当前主题保持一致。
 *
 * 主要解决的问题：
 * - 在 Activity 重建后，状态栏和导航栏的图标颜色可能没有正确应用
 * - 特别是在深色/浅色主题切换时，系统栏颜色可能不正确
 *
 */
@RequiresApi(Build.VERSION_CODES.M)
object SystemBarAppearancePatch : ActivityListener {
    override fun onActivityPostCreate(activity: Activity, savedInstanceState: Bundle?) {
        activity.apply {
            window.setSystemBarsAppearance(
                isLightStatusBar = if (isLightStatusBarSupported) {
                    theme.getBoolean(android.R.attr.windowLightStatusBar, false)
                } else {
                    false
                },
                isLightNavigationBar = if (isLightNavigationBarResSupported) {
                    theme.getBoolean(android.R.attr.windowLightNavigationBar, false)
                } else {
                    false
                }
            )
        }
    }
}