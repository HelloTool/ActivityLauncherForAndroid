package io.gitee.jesse205.activitylauncher.features.applist

import android.app.Application
import android.os.Build
import android.os.Parcelable
import androidx.annotation.RequiresApi
import io.gitee.jesse205.activitylauncher.app.BaseActivityState
import io.gitee.jesse205.activitylauncher.utils.AppProvisionType
import io.gitee.jesse205.activitylauncher.utils.AppSortCategory
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.concurrent.Executors


@Parcelize
class MainActivityState(
    private var _sortCategory: AppSortCategory,
    private var _provisionType: AppProvisionType
) : BaseActivityState<MainActivityState.MainActivityStateListener>(), Parcelable {

    @IgnoredOnParcel
    private var loadAppsTask: LoadAppsTask? = null

    @IgnoredOnParcel
    private var sortAppsTask: SortAppsTask? = null

    @IgnoredOnParcel
    private var loadAppNamesTask: LoadAppNamesTask? = null

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
    var apps: List<AppItem>? = null

    @IgnoredOnParcel
    var sortedApps: List<AppItem>? = null
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

    @IgnoredOnParcel
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

    override fun destroy() {
        super.destroy()
        ignoreAndCancelTasks()
    }

    interface MainActivityStateListener {
        fun onAppSortCategoryUpdate(sortCategory: AppSortCategory)
        fun onAppProvisionTypeUpdate(provisionType: AppProvisionType)
        fun onAppsLoadingUpdate(isAppsLoading: Boolean)
        fun onAppNamesLoadingUpdate(isAppNamesLoading: Boolean)
        fun onSortedAppsUpdate(apps: List<AppItem>?)
    }
}