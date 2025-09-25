package io.gitee.jesse205.activitylauncher.utils.tabs

import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TabHost
import android.widget.TabWidget
import io.gitee.jesse205.activitylauncher.R


@Suppress("DEPRECATION")
fun View.wrapTabWidget(): TabHost {
    val viewParent: ViewParent? = parent
    if (viewParent is ViewGroup) {
        viewParent.removeView(this)
    }

    return TabHost(context, null).apply {
        id = android.R.id.tabhost
        this@wrapTabWidget.layoutParams?.let {
            layoutParams = it
        }

        // 添加一个 tabcontent 和 tab_content_item，使 TabHost 正常工作
        FrameLayout(context).apply {
            id = android.R.id.tabcontent
            visibility = View.GONE

            View(context).apply {
                id = R.id.tab_content_item
            }.also { addView(it) }

        }.also { addView(it) }

        LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            orientation = LinearLayout.VERTICAL

            TabWidget(context).apply {
                id = android.R.id.tabs
            }.also { addView(it) }

            this@wrapTabWidget.apply {
                setLayoutParams(
                    LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )
            }.also { addView(it) }


        }.also { addView(it) }

    }.also {
        if (viewParent is ViewGroup) {
            viewParent.addView(it)
        }
    }
}