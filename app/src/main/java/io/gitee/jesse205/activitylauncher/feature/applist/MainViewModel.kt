package io.gitee.jesse205.activitylauncher.feature.applist

import android.app.Application
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.RequiresApi
import io.gitee.jesse205.activitylauncher.app.BaseViewModel
import io.gitee.jesse205.activitylauncher.util.AppProvisionType
import io.gitee.jesse205.activitylauncher.util.AppSortCategory
import io.gitee.jesse205.activitylauncher.util.getParcelableCompat
import kotlinx.parcelize.Parcelize
import java.util.concurrent.Executors


class MainViewModel(
    sortCategory: AppSortCategory,
    provisionType: AppProvisionType
) : BaseViewModel<MainViewModel.MainActivityStateListener>() {

    private var loadAppsTask: LoadAppsTask? = null

    private var sortAppsTask: SortAppsTask? = null

    private var loadAppNamesTask: LoadAppNamesTask? = null

    var sortCategory: AppSortCategory = sortCategory
        private set(value) {
            field = value
            listeners.forEach { it.onAppSortCategoryUpdate(value) }
        }

    var provisionType: AppProvisionType = provisionType
        private set(value) {
            field = value
            listeners.forEach { it.onAppProvisionTypeUpdate(value) }
        }

    var apps: List<AppItem>? = null


    var sortedApps: List<AppItem>? = null
        private set(value) {
            field = value
            listeners.forEach { it.onSortedAppsUpdate(value) }
        }


    var isAppsLoading = false
        private set(value) {
            field = value
            listeners.forEach { it.onAppsLoadingUpdate(value) }
        }

    val isAppsLoadingOrLoaded get() = isAppsLoading || sortedApps != null

    var isAppNamesLoading = false
        private set(value) {
            field = value
            listeners.forEach { it.onAppNamesLoadingUpdate(value) }
        }

    private fun ignoreAndCancelTasks() {
        @Suppress("DEPRECATION")
        run {
            loadAppsTask?.apply {
                ignore()
                cancel(true)
            }
            sortAppsTask?.apply {
                ignore()
                cancel(true)
            }
            loadAppNamesTask?.apply {
                ignore()
                cancel(true)
            }
        }
    }

    fun loadApps(application: Application) {
        ignoreAndCancelTasks()

        loadAppsTask = LoadAppsTask(
            application = application,
            provisionType = provisionType,
            onBeforeLoad = {
                isAppsLoading = true
                apps = null
            },
            onLoad = {
                apps = it
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    loadAppNames(application)
                }
                sortApps(application)
            },
            onCancel = {
                isAppsLoading = false
            }
        ).apply {
            @Suppress("DEPRECATION")
            execute()
        }
    }

    private fun sortApps(application: Application) {
        val currentApps = apps ?: return
        @Suppress("DEPRECATION")
        sortAppsTask?.apply {
            ignore()
            cancel(true)
        }
        sortAppsTask = SortAppsTask(
            application = application,
            apps = currentApps,
            sortCategory = sortCategory,
            onBeforeSort = {
                isAppsLoading = true
                sortedApps = null
            },
            onSort = {
                isAppsLoading = false
                sortedApps = it
            },
            onCancel = {
                isAppsLoading = false
            }
        ).apply {
            @Suppress("DEPRECATION")
            execute()
        }
    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    private fun loadAppNames(application: Application) {
        val currentApps = apps ?: return
        @Suppress("DEPRECATION")
        loadAppNamesTask?.apply {
            ignore()
            cancel(true)
        }
        loadAppNamesTask = LoadAppNamesTask(
            application = application,
            apps = currentApps,
            onBeforeLoad = {
                isAppNamesLoading = true
            },
            onLoad = {
                isAppNamesLoading = false
            },
            onCancel = {
                isAppNamesLoading = false
            }
        ).apply {
            @Suppress("DEPRECATION")
            executeOnExecutor(Executors.newSingleThreadExecutor())
        }
    }

    fun changeAppSortCategory(application: Application, sortCategory: AppSortCategory) {
        this.sortCategory = sortCategory
        sortApps(application)
    }

    fun changeAppProvisionType(application: Application, provisionType: AppProvisionType) {
        this.provisionType = provisionType
        loadApps(application)
    }


    override fun saveHierarchyState() = Bundle().apply {
        putParcelable(
            SAVED_STATE_TAG,
            SavedState(
                sortCategory = sortCategory,
                provisionType = provisionType
            )
        )
    }

    override fun restoreHierarchyState(state: Bundle?) {
        state?.getParcelableCompat(SAVED_STATE_TAG, SavedState::class.java)?.let {
            sortCategory = it.sortCategory
            provisionType = it.provisionType
        }
    }


    override fun destroy() {
        super.destroy()
        ignoreAndCancelTasks()
    }

    @Parcelize
    data class SavedState(
        val sortCategory: AppSortCategory,
        val provisionType: AppProvisionType
    ) : Parcelable

    interface MainActivityStateListener {
        fun onAppSortCategoryUpdate(sortCategory: AppSortCategory)
        fun onAppProvisionTypeUpdate(provisionType: AppProvisionType)
        fun onAppsLoadingUpdate(isAppsLoading: Boolean)
        fun onAppNamesLoadingUpdate(isAppNamesLoading: Boolean)
        fun onSortedAppsUpdate(apps: List<AppItem>?)
    }

    companion object {
        const val SAVED_STATE_TAG = "savedState"
    }
}