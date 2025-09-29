package io.gitee.jesse205.activitylauncher.features.applist

import android.app.Application
import android.content.pm.PackageManager
import android.os.AsyncTask
import io.gitee.jesse205.activitylauncher.model.LoadedAppInfo
import io.gitee.jesse205.activitylauncher.utils.AppProvisionType
import io.gitee.jesse205.activitylauncher.utils.AppSortCategory
import io.gitee.jesse205.activitylauncher.utils.appProvisionType

@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
class LoadAppsTask(
    application: Application,
    private val sortCategory: AppSortCategory,
    private val provisionType: AppProvisionType,
    private val onBeforeLoad: () -> Unit,
    private val onLoad: (List<LoadedAppInfo>) -> Unit,
    private val onCancel: () -> Unit,
) : AsyncTask<Void, Void, List<LoadedAppInfo>>() {
    private val packageManager: PackageManager = application.packageManager
    private var isTaskIgnored = false
    override fun doInBackground(vararg params: Void): List<LoadedAppInfo>? {
        return packageManager.getInstalledPackages(0)
            .filter { it.applicationInfo != null && it.appProvisionType == provisionType }
            .map { Pair(it, LoadedAppInfo(applicationInfo = it.applicationInfo!!)) }
            .sortedWith { a, b ->
                when (sortCategory) {
                    AppSortCategory.NAME -> a.second.loadLabel(packageManager).toString()
                        .compareTo(b.second.loadLabel(packageManager).toString(), true)

                    AppSortCategory.INSTALL_TIME -> b.first.firstInstallTime.compareTo(a.first.firstInstallTime)
                    AppSortCategory.UPDATE_TIME -> b.first.lastUpdateTime.compareTo(a.first.lastUpdateTime)
                }
            }
            .map { it.second }
    }

    override fun onPreExecute() {
        if (!isTaskIgnored) {
            onBeforeLoad()
        }
    }

    override fun onPostExecute(result: List<LoadedAppInfo>) {
        if (!isTaskIgnored) {
            onLoad(result)
        }
    }

    override fun onCancelled() {
        if (!isTaskIgnored) {
            onCancel()
        }
    }

    fun ignore() {
        isTaskIgnored = true
    }

    companion object {
        const val TAG = "LoadAppsTask"
    }
}