package io.gitee.jesse205.activitylauncher.utils

import android.view.View
import android.view.ViewGroup
import android.view.ViewParent

/**
 * 创建一个迭代器，用于迭代指定视图的所有父级视图
 */
fun View.parentsIterator(): Iterator<ViewParent> {
    return ParentViewIterator(this)
}

/**
 * 用于迭代View的所有父级视图的迭代器
 */
private class ParentViewIterator(private val view: View) : Iterator<ViewParent> {
    private var current: ViewParent? = null
    override fun hasNext(): Boolean {
        val current = current
        return (if (current != null) current.parent else view.parent) != null
    }

    override fun next(): ViewParent {
        val current = current
        val parent = (if (current != null) current.parent else view.parent) ?: throw NoSuchElementException()
        this.current = parent
        return parent
    }
}


fun View.parentsDoNotClipChildrenAndPadding(rootLayout: ViewGroup) {
    for (parent in parentsIterator()) {
        if (parent !is ViewGroup) {
            break
        }
        parent.clipChildren = false
        parent.clipToPadding = false
        if (parent == rootLayout) {
            break
        }
    }
}