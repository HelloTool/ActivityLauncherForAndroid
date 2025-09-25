package io.gitee.jesse205.activitylauncher.utils.tabs

import android.app.ActionBar
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.HONEYCOMB)
fun ActionBar.tabsIterator(): ActionBarTabsIterator {
    return ActionBarTabsIterator(this)
}