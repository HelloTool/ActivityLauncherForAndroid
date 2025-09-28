package io.gitee.jesse205.activitylauncher.utils.tabs

import android.app.Activity
import android.os.Build
import android.view.View
import io.gitee.jesse205.activitylauncher.utils.isEmui

object TabControllerFactory {
    fun create(activity: Activity, rootView: View, onSelect: (tabTag: String) -> Unit): TabController {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.actionBar?.let {
                return ActionBarTabController(it, onSelect)
            }
        }
        return TabHostController(rootView, onSelect)
    }
}