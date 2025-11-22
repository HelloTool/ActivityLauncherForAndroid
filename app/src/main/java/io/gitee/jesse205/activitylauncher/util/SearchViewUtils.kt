package io.gitee.jesse205.activitylauncher.util

import android.widget.SearchView

/**
 * 临时清除焦点，用于防止窗口获得焦点时被 SearchView 拉起输入法
 */
fun SearchView.temporarilyClearFocus() {
    if (hasFocus()) {
        clearFocus()
        post {
            requestFocus()
        }
    }
}