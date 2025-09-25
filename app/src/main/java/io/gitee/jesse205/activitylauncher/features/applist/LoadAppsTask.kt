package io.gitee.jesse205.activitylauncher.features.applist

import android.app.Application
import android.content.pm.PackageManager
import android.util.Log
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
                AppSortCategory.NAME -> a.loadLabel(packageManager).toString().compareTo(b.label.toString())
                AppSortCategory.INSTALL_TIME -> b.packageInfo.firstInstallTime.compareTo(a.packageInfo.firstInstallTime)
                AppSortCategory.UPDATE_TIME -> b.packageInfo.lastUpdateTime.compareTo(a.packageInfo.lastUpdateTime)
            }
        }
    }

    override fun onPreExecute() {
        weakActivity?.apply {
            setLoadingApps(true)
            setApps(listOf())
        }
        state.apply {
            isLoadingApps = true
            apps = listOf()
            loadAppsTask = this@LoadAppsTask
        }
    }

    override fun onPostExecute(result: List<LoadedAppInfo>) {
        if (!isCurrentTaskRunning) return
        weakActivity?.apply {
            setLoadingApps(false)
            setApps(result)
        }
        state.apply {
            isLoadingApps = false
            apps = result
            loadAppsTask = null
        }
    }

    override fun onCancelled() {
        if (!isCurrentTaskRunning) return
        weakActivity?.takeIf { !it.isFinishing }?.apply {
            setLoadingApps(false)
        }

        state.apply {
            isLoadingApps = false
            loadAppsTask = null
        }
    }
}