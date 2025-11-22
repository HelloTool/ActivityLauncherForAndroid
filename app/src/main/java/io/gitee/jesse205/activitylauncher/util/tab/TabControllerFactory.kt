package io.gitee.jesse205.activitylauncher.util.tab

import android.app.Activity
import android.os.Build
import android.view.View

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