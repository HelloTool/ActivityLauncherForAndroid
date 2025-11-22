package io.gitee.jesse205.activitylauncher.widget

import android.content.Context
import android.util.AttributeSet
import android.view.ContextMenu
import android.widget.GridView
import io.gitee.jesse205.activitylauncher.util.adapterview.ContextMenuInfoGettable

class AdvancedGridView : GridView {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun getContextMenuInfo(): ContextMenu.ContextMenuInfo? {
        val superContextMenuInfo = super.getContextMenuInfo()
        val adapter = adapter
        val adapterContextMenuInfo =
            if (superContextMenuInfo is AdapterContextMenuInfo && adapter is ContextMenuInfoGettable)
                adapter.getContextMenuInfo(superContextMenuInfo.position, superContextMenuInfo.id)
            else null
        return adapterContextMenuInfo ?: superContextMenuInfo
    }
}