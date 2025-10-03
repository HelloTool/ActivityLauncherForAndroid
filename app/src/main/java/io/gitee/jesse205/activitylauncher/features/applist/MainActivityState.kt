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
        private set(value) {
            field = value
            listeners.forEach { it.onAppsUpdate(value) }
        }

    @IgnoredOnParcel
    var isAppsLoading = false
        private set(value) {
            field = value
            listeners.forEach { it.onAppsLoadingUpdate(value) }
        }

    val isAppsLoadingOrLoaded get() = isAppsLoading || apps != null

    fun loadApps(application: Application) {
        loadAppsTask?.apply {
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
                apps = listOf()
            },
            onLoad = {
                isAppsLoading = false
                apps = it
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
        loadApps(application)
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
        fun onAppsUpdate(apps: List<LoadedAppInfo>?)
    }
}