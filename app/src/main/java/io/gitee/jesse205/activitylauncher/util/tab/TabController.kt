package io.gitee.jesse205.activitylauncher.util.tab

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

abstract class TabController(private val onSelect: (tabTag: String) -> Unit) {
    protected var skipOnTabChangedListenerDepth = 0

    @OptIn(ExperimentalContracts::class)
    protected fun withSkipTabChangedListener(block: () -> Unit) {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }
        skipOnTabChangedListenerDepth += 1
        block()
        skipOnTabChangedListenerDepth -= 1
    }

    protected fun notifyTabChanged(tabTag: String) {
        if (skipOnTabChangedListenerDepth > 0) return
        onSelect(tabTag)
    }

    abstract fun setup()
    abstract fun setCurrentTab(tabTag: String)
    abstract fun addTab(tabTag: String, @StringRes textId: Int?, @DrawableRes tabIconId: Int?)
}