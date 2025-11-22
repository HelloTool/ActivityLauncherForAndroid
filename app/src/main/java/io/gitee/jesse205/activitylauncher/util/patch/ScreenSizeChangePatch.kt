package io.gitee.jesse205.activitylauncher.util.patch

import android.app.Activity
import android.content.res.Configuration
import android.os.Build
import androidx.annotation.RequiresApi
import io.gitee.jesse205.activitylauncher.util.ActivityListener
import io.gitee.jesse205.activitylauncher.util.ScopedActivityListenerManager
import io.gitee.jesse205.activitylauncher.util.screenLayoutSize
import io.gitee.jesse205.activitylauncher.util.tryRecreateCompat

/**
 * 屏幕尺寸变化修复补丁
 *
 * 该对象用于在 Android 7.0 (API 24) 及以上版本中处理屏幕尺寸变化的兼容性问题，
 * 特别是在华为平行视界（EasyGo）环境下确保 Activity 能够正常重建。
 *
 * 主要功能：
 * - 监听屏幕尺寸变化
 * - 在适当的时候重建 Activity 以适配新的屏幕尺寸
 * - 解决在多窗口模式下屏幕尺寸变化时 Activity 无法正确重建的问题
 */
@RequiresApi(Build.VERSION_CODES.N)
object ScreenSizeChangePatch :
    ScopedActivityListenerManager<ScreenSizeChangePatch.ScreenSizeChangePatchActivityListener>() {
    override fun createActivityScopeListener(activity: Activity) = ScreenSizeChangePatchActivityListener(activity)

    class ScreenSizeChangePatchActivityListener(activity: Activity) : ActivityListener {
        val screenLayoutSizeWhenCreated: Int = activity.resources.configuration.screenLayoutSize
        var isResumed: Boolean = false
        override fun onActivityResume(activity: Activity) {
            isResumed = true
            if (activity.resources.configuration.screenLayoutSize != screenLayoutSizeWhenCreated) {
                activity.tryRecreateCompat()
            }
        }

        override fun onActivityPause(activity: Activity) {
            isResumed = false
        }

        override fun onActivityConfigurationChanged(activity: Activity, newConfig: Configuration) {
            if (isResumed && newConfig.screenLayoutSize != screenLayoutSizeWhenCreated) {
                activity.tryRecreateCompat()
            }
        }
    }
}