package io.gitee.jesse205.activitylauncher.utils

import android.app.Activity
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
class EasyGoPatch(val activity: Activity) : ActivityListener {

    private var isInMultiWindowModeWhenCreated: Boolean = false
    private var screenLayoutSizeWhenCreated: Int = 0
    private var isResumed = false
    override fun onCreate(savedInstanceState: Bundle?) {
        isInMultiWindowModeWhenCreated = activity.isInMultiWindowMode
        screenLayoutSizeWhenCreated =
            activity.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
    }

    override fun onResume() {
        isResumed = true
        if (getScreenLayoutSize() != screenLayoutSizeWhenCreated) {
            activity.recreate()
        }
    }

    override fun onPause() {
        isResumed = false
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        if (isResumed && getScreenLayoutSize(newConfig) != screenLayoutSizeWhenCreated) {
            activity.recreate()
        }
    }

    private fun getScreenLayoutSize(configuration: Configuration = activity.resources.configuration): Int {
        return configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
    }
}