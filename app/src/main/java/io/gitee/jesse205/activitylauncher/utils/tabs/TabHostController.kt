@file:Suppress("DEPRECATION")

package io.gitee.jesse205.activitylauncher.utils.tabs

import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TabHost
import android.widget.TabWidget
import io.gitee.jesse205.activitylauncher.R

class TabHostController(val rootView: View, onSelect: (tabTag: String) -> Unit) : TabController(onSelect) {
    lateinit var tabHost: TabHost

    override fun setup() {
        val viewParent: ViewParent? = rootView.parent
        if (viewParent is ViewGroup) {
            viewParent.removeView(rootView)
        }

        tabHost = TabHost(rootView.context, null).apply {
            id = android.R.id.tabhost
            this@TabHostController.rootView.layoutParams?.let {
                layoutParams = it
            }
            setOnTabChangedListener(this@TabHostController::notifyTabChanged)

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

                this@TabHostController.rootView.apply {
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
            it.setup()
        }
    }

    override fun setCurrentTab(tabTag: String) {
        withSkipTabChangedListener {
            tabHost.setCurrentTabByTag(tabTag)
        }
    }

    override fun addTab(tabTag: String, textId: Int?, tabIconId: Int?) {
        withSkipTabChangedListener {
            tabHost.newTabSpec(tabTag)
                .setIndicator(
                    textId?.let { tabHost.resources.getString(it) },
                    tabIconId?.let { tabHost.resources.getDrawable(it) }
                )
                .setContent(R.id.tab_content_item)
                .also { tabHost.addTab(it) }
        }
    }
}