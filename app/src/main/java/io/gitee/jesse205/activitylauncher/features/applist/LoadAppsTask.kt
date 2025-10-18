@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package io.gitee.jesse205.activitylauncher.features.applist

import android.app.Application
import android.content.pm.PackageManager
import android.os.AsyncTask
import io.gitee.jesse205.activitylauncher.utils.AppProvisionType
import io.gitee.jesse205.activitylauncher.utils.AppSortCategory
import io.gitee.jesse205.activitylauncher.utils.appProvisionType

class LoadAppsTask(
    application: Application,
    private val sortCategory: AppSortCategory,
    private val provisionType: AppProvisionType,
    private val onBeforeLoad: () -> Unit,
    private val onLoad: (List<AppModel>) -> Unit,
    private val onCancel: () -> Unit,
) : AsyncTask<Void, Void, List<AppModel>>() {
    private val packageManager: PackageManager = application.packageManager
    private var isTaskIgnored = false
    override fun doInBackground(vararg params: Void): List<AppModel>? {
        return packageManager.getInstalledPackages(0)
            .filter { it.applicationInfo != null && it.appProvisionType == provisionType }
            .map {
                AppModel(
                    applicationInfo = it.applicationInfo!!,
                    firstInstallTime = it.firstInstallTime,
                    lastUpdateTime = it.lastUpdateTime,
                )
            }

    }

    override fun onPreExecute() {
        if (!isTaskIgnored) {
            onBeforeLoad()
        }
    }

    override fun onPostExecute(result: List<AppModel>) {
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