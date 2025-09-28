package io.gitee.jesse205.activitylauncher.features.applist

import android.app.Application
import android.content.pm.PackageManager
import io.gitee.jesse205.activitylauncher.core.BaseActivityAsyncTask
import io.gitee.jesse205.activitylauncher.model.LoadedAppInfo
import io.gitee.jesse205.activitylauncher.utils.AppSortCategory
import io.gitee.jesse205.activitylauncher.utils.appProvisionType

@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
class LoadAppsTask(
    val application: Application,
    activity: MainActivity,
    state: MainActivityState
) :
    BaseActivityAsyncTask<MainActivity, MainActivityState, Void, Void, List<LoadedAppInfo>>(activity, state) {
    val packageManager: PackageManager = application.packageManager
    val isCurrentTaskRunning get() = state.loadAppsTask == this

    override fun doInBackground(vararg params: Void): List<LoadedAppInfo>? {
        return packageManager.getInstalledPackages(0).filter {
            it.applicationInfo != null && it.appProvisionType == state.provisionType
        }.map {
            LoadedAppInfo(
                applicationInfo = it.applicationInfo!!,
                packageInfo = it
            )
        }.sortedWith { a, b ->
            when (state.sortCategory) {
                AppSortCategory.NAME -> a.loadLabel(packageManager).toString()
                    .compareTo(b.loadLabel(packageManager).toString(), true)

                AppSortCategory.INSTALL_TIME -> b.packageInfo.firstInstallTime.compareTo(a.packageInfo.firstInstallTime)
                AppSortCategory.UPDATE_TIME -> b.packageInfo.lastUpdateTime.compareTo(a.packageInfo.lastUpdateTime)
            }
        }
    }

    override fun onPreExecute() {
        state.apply {
            isAppsLoading = true
            apps = listOf()
            loadAppsTask = this@LoadAppsTask
        }
        weakActivity?.apply {
            refreshAppsLoading()
            refreshApps()
        }
    }

    override fun onPostExecute(result: List<LoadedAppInfo>) {
        if (!isCurrentTaskRunning) return

        state.apply {
            isAppsLoading = false
            apps = result
            loadAppsTask = null
        }
        weakActivity?.apply {
            refreshAppsLoading()
            refreshApps()
        }
    }

    override fun onCancelled() {
        if (!isCurrentTaskRunning) return
        state.apply {
            isAppsLoading = false
            loadAppsTask = null
        }
        weakActivity?.takeIf { !it.isFinishing }?.apply {
            refreshAppsLoading()
        }
    }

    companion object {
        const val TAG = "LoadAppsTask"
    }
}