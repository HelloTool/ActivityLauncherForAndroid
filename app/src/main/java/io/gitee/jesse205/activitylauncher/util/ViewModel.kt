package io.gitee.jesse205.activitylauncher.util

import android.app.Activity
import android.os.Bundle
import io.gitee.jesse205.activitylauncher.app.BaseActivity
import kotlin.reflect.KProperty

interface ViewModelStore {
    fun <T : ViewModel<*>> getViewModel(clazz: Class<T>): T?
    fun <T : ViewModel<*>> setViewModel(clazz: Class<T>, value: T)
}

inline fun <L, reified T : ViewModel<L>> BaseActivity.viewModel(
    listener: L,
    noinline initializer: () -> T
) = ViewModelDelegate(
    clazz = T::class.java,
    store = this,
    activityListenable = this,
    listener = listener,
    initializer = initializer
)

interface ViewModel<StateListener> : Listenable<StateListener> {
    fun destroy()
    fun saveHierarchyState(): Bundle? = null
    fun restoreHierarchyState(state: Bundle?) {}
}

class ViewModelDelegate<L, T : ViewModel<L>>(
    val clazz: Class<T>,
    val store: ViewModelStore,
    val activityListenable: Listenable<ActivityListener>,
    val listener: L,
    val initializer: () -> T
) {

    private var value: T? = null
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        value?.let {
            return it
        }

        val viewModel = store.getViewModel(clazz) ?: initializer()
        return viewModel.also {
            viewModel.addListener(listener)
            activityListenable.addListener(object : ActivityListener {
                override fun onActivityDestroy(activity: Activity) {
                    viewModel.removeListener(listener)
                }
            })
            value = it
        }
    }

}