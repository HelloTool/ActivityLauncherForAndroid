package io.gitee.jesse205.activitylauncher.util.adapterview

import android.view.ContextMenu

interface ContextMenuInfoGettable {
    fun getContextMenuInfo(position: Int, itemId: Long): ContextMenu.ContextMenuInfo?
}