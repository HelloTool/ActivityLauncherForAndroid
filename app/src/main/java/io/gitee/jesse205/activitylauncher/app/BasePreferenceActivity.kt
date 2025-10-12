@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package io.gitee.jesse205.activitylauncher.app

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceActivity
import android.view.ViewGroup
import io.gitee.jesse205.activitylauncher.utils.ActivityListener
import io.gitee.jesse205.activitylauncher.utils.Listenable
import io.gitee.jesse205.activitylauncher.utils.isNavigationGestureSupported
import io.gitee.jesse205.activitylauncher.utils.isSupportEdgeToEdge
import io.gitee.jesse205.activitylauncher.utils.parentsDoNotClipChildrenAndPadding

open class BasePreferenceActivity : PreferenceActivity(), Listenable<ActivityListener> {
    private var listeners: MutableList<ActivityListener> = mutableListOf()

    private val helper = BaseActivityHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        helper.onActivityPreCreate(savedInstanceState)
        super.onCreate(savedInstanceState)
        helper.onActivityCreate(savedInstanceState)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        helper.onActivityPostCreate(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        helper.onActivitySaveInstanceState(outState)
    }

    override fun onMultiWindowModeChanged(isInMultiWindowMode: Boolean, newConfig: Configuration) {
        super.onMultiWindowModeChanged(isInMultiWindowMode, newConfig)
        helper.onActivityMultiWindowModeChanged(isInMultiWindowMode, newConfig)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        helper.onActivityConfigurationChanged(newConfig)
    }

    override fun onContentChanged() {
        super.onContentChanged()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            val rootLayout = window.decorView.findViewById<ViewGroup>(android.R.id.content).getChildAt(0) as ViewGroup
            rootLayout.apply {
                fitsSystemWindows = true
            }
            if (isNavigationGestureSupported && theme.isSupportEdgeToEdge) {
                listView.parentsDoNotClipChildrenAndPadding(rootLayout)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        helper.onActivityStart()
    }

    override fun onResume() {
        super.onResume()
        helper.onActivityResume()
    }

    override fun onPause() {
        super.onPause()
        helper.onActivityPause()
    }

    override fun onStop() {
        super.onStop()
        helper.onActivityStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        helper.onActivityDestroy()
    }

    override fun addListener(listener: ActivityListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: ActivityListener) {
        listeners.remove(listener)
    }
}