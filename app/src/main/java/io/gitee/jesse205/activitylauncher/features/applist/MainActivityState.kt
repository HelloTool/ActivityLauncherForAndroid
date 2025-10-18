package io.gitee.jesse205.activitylauncher.features.applist

import android.app.Application
import android.os.Parcelable
import io.gitee.jesse205.activitylauncher.app.BaseActivityState
import io.gitee.jesse205.activitylauncher.model.LoadedAppInfo
import io.gitee.jesse205.activitylauncher.utils.AppProvisionType
import io.gitee.jesse205.activitylauncher.utils.AppSortCategory
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize


@Parcelize
class MainActivityState(
    private var _sortCategory: AppSortCategory,
    private var _provisionType: AppProvisionType
) : BaseActivityState<MainActivityState.MainActivityStateListener>(), Parcelable {

    @IgnoredOnParcel
    private var loadAppsTask: LoadAppsTask? = null

    @IgnoredOnParcel
    private var sortAppsTask: SortAppsTask? = null

    var sortCategory: AppSortCategory
        get() = _sortCategory
        private set(value) {
            _sortCategory = value
            listeners.forEach { it.onAppSortCategoryUpdate(value) }
        }

    var provisionType: AppProvisionType
        get() = _provisionType
        private set(value) {
            _provisionType = value
            listeners.forEach { it.onAppProvisionTypeUpdate(value) }
        }

    @IgnoredOnParcel
    var apps: List<LoadedAppInfo>? = null

    @IgnoredOnParcel
    var sortedApps: List<LoadedAppInfo>? = null
        private set(value) {
            field = value
            listeners.forEach { it.onSortedAppsUpdate(value) }
        }


    @IgnoredOnParcel
    var isAppsLoading = false
        private set(value) {
            field = value
            listeners.forEach { it.onAppsLoadingUpdate(value) }
        }

    val isAppsLoadingOrLoaded get() = isAppsLoading || sortedApps != null

    fun loadApps(application: Application) {
        loadAppsTask?.apply {
            ignore()
            @Suppress("DEPRECATION")
            cancel(true)
        }
        sortAppsTask?.apply {
            ignore()
            @Suppress("DEPRECATION")
            cancel(true)
        }
        loadAppsTask = LoadAppsTask(
            application = application,
            sortCategory = sortCategory,
            provisionType = provisionType,
            onBeforeLoad = {
                isAppsLoading = true
                apps = null
            },
            onLoad = {
                apps = it
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
        sortAppsTask?.apply {
            ignore()
            @Suppress("DEPRECATION")
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

    fun changeAppSortCategory(application: Application, sortCategory: AppSortCategory) {
        this.sortCategory = sortCategory
        sortApps(application)
    }

    fun changeAppProvisionType(application: Application, provisionType: AppProvisionType) {
        this.provisionType = provisionType
        loadApps(application)
    }

    override fun destroy() {
        super.destroy()
        loadAppsTask?.apply {
            ignore()
            @Suppress("DEPRECATION")
            cancel(true)
        }
    }

    interface MainActivityStateListener {
        fun onAppSortCategoryUpdate(sortCategory: AppSortCategory)
        fun onAppProvisionTypeUpdate(provisionType: AppProvisionType)
        fun onAppsLoadingUpdate(isAppsLoading: Boolean)
        fun onSortedAppsUpdate(apps: List<LoadedAppInfo>?)
    }
}