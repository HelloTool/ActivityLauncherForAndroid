package io.gitee.jesse205.activitylauncher.utils.patches

import android.app.Activity
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import io.gitee.jesse205.activitylauncher.utils.ActivityListener
import io.gitee.jesse205.activitylauncher.utils.screenLayoutSize
import java.util.WeakHashMap

/**
 * 使在 EasyGo 下也能正常重载 Activity
 */
@RequiresApi(Build.VERSION_CODES.N)
class EasyGoPatch : ActivityListener {
    private val stateMap: MutableMap<Activity, EasyGoPatchState> = WeakHashMap()

    override fun onActivityCreate(activity: Activity, savedInstanceState: Bundle?) {
        stateMap[activity] = EasyGoPatchState(
            isInMultiWindowModeWhenCreated = activity.isInMultiWindowMode,
            screenLayoutSizeWhenCreated = activity.resources.configuration.screenLayoutSize,
            isResumed = false
        )
    }

    override fun onActivityResume(activity: Activity) {
        val state = stateMap[activity] ?: return
        state.isResumed = true
        if (activity.resources.configuration.screenLayoutSize != state.screenLayoutSizeWhenCreated) {
            activity.recreate()
        }
    }

    override fun onActivityPause(activity: Activity) {
        val state = stateMap[activity] ?: return
        state.isResumed = false
    }

    override fun onActivityConfigurationChanged(activity: Activity, newConfig: Configuration) {
        val state = stateMap[activity] ?: return
        if (state.isResumed && newConfig.screenLayoutSize != state.screenLayoutSizeWhenCreated) {
            activity.recreate()
        }
    }

    override fun onActivityDestroy(activity: Activity) {
        stateMap.remove(activity)
    }

    data class EasyGoPatchState(
        var isInMultiWindowModeWhenCreated: Boolean,
        var screenLayoutSizeWhenCreated: Int,
        var isResumed: Boolean
    )
}