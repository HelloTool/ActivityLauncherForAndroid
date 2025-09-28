package io.gitee.jesse205.activitylauncher.utils

import android.app.ActionBar
import android.os.Build
import android.view.MenuItem
import androidx.annotation.RequiresApi

/**
 * 修复启用返回按钮时候工具栏多出边距，通过临时关闭返回按钮解决
 */
@RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
class CollapseActionViewMenuItemPatch(val actionBar: ActionBar) : MenuItem.OnActionExpandListener {
    var isDisplayHomeAsUpEnabled = false
    override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
        if (isDisplayHomeAsUpEnabled) {
            actionBar.displayOptions = actionBar.displayOptions or ActionBar.DISPLAY_HOME_AS_UP
        }
        return true
    }

    override fun onMenuItemActionExpand(item: MenuItem): Boolean {
        isDisplayHomeAsUpEnabled = actionBar.displayOptions and ActionBar.DISPLAY_HOME_AS_UP != 0
        if (isDisplayHomeAsUpEnabled) {
            actionBar.displayOptions = actionBar.displayOptions and ActionBar.DISPLAY_HOME_AS_UP.inv()
        }

        return true
    }
}