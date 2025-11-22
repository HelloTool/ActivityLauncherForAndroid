@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package io.gitee.jesse205.activitylauncher.util.tab

import android.app.ActionBar
import android.app.FragmentTransaction
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.HONEYCOMB)
class ActionBarTabController(val actionBar: ActionBar, onSelect: (tabTag: String) -> Unit) : TabController(onSelect) {
    private val actionBarTabListener = object : ActionBar.TabListener {
        override fun onTabReselected(tab: ActionBar.Tab, ft: FragmentTransaction) {

        }

        override fun onTabSelected(tab: ActionBar.Tab, ft: FragmentTransaction) {
            notifyTabChanged(tab.tag.toString())
        }

        override fun onTabUnselected(tab: ActionBar.Tab, ft: FragmentTransaction) {
        }
    }

    override fun setup() {
        actionBar.navigationMode = ActionBar.NAVIGATION_MODE_TABS
    }

    override fun setCurrentTab(tabTag: String) {
        withSkipTabChangedListener {
            for (tab in actionBar.tabsIterator()) {
                if (tab.tag == tabTag) {
                    actionBar.selectTab(tab)
                    break
                }
            }
        }
    }

    override fun addTab(tabTag: String, textId: Int?, tabIconId: Int?) {
        withSkipTabChangedListener {
            actionBar.newTab().apply {
                tag = tabTag
                if (textId != null) {
                    setText(textId)
                }
                if (tabIconId != null) {
                    setIcon(tabIconId)
                }
                setTabListener(actionBarTabListener)
            }.also {
                actionBar.addTab(it)
            }
        }
    }
}