package io.gitee.jesse205.activitylauncher.utils.patches

import android.app.Activity
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import io.gitee.jesse205.activitylauncher.utils.ActivityListener
import io.gitee.jesse205.activitylauncher.utils.getBoolean
import io.gitee.jesse205.activitylauncher.utils.isLightNavigationBarResSupported
import io.gitee.jesse205.activitylauncher.utils.isLightStatusBarSupported
import io.gitee.jesse205.activitylauncher.utils.setSystemBarsAppearance

/**
 * 系统栏外观补丁。在 Android 8.0 中，recreate 不会重新设置系统栏外观，因此需要手动设置。
 */
@RequiresApi(Build.VERSION_CODES.M)
object SystemBarAppearancePatch : ActivityListener {
    override fun onActivityPostCreate(activity: Activity, savedInstanceState: Bundle?) {
        activity.apply {
            setSystemBarsAppearance(
                isLightSystemBars = if (isLightStatusBarSupported) {
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