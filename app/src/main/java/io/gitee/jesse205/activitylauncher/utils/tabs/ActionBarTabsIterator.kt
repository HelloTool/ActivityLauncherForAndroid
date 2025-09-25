package io.gitee.jesse205.activitylauncher.utils.tabs

import android.app.ActionBar
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.HONEYCOMB)
class ActionBarTabsIterator(val actionBar: ActionBar) : Iterator<ActionBar.Tab> {
    private var currentIndex = 0
    override fun next(): ActionBar.Tab {
        val tab = actionBar.getTabAt(currentIndex)
        currentIndex++
        return tab
    }

    override fun hasNext(): Boolean {
        return currentIndex < actionBar.tabCount
    }
}