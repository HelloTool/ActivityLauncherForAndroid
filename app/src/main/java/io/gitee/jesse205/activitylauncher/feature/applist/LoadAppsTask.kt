@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package io.gitee.jesse205.activitylauncher.feature.applist

import android.app.Application
import android.content.pm.PackageManager
import android.os.AsyncTask
import io.gitee.jesse205.activitylauncher.util.AppProvisionType
import io.gitee.jesse205.activitylauncher.util.appProvisionType

class LoadAppsTask(
    application: Application,
    private val provisionType: AppProvisionType,
    private val onBeforeLoad: () -> Unit,
    private val onLoad: (List<AppItem>) -> Unit,
    private val onCancel: () -> Unit,
) : AsyncTask<Void, Void, List<AppItem>>() {
    private val packageManager: PackageManager = application.packageManager
    private var isTaskIgnored = false
    override fun doInBackground(vararg params: Void): List<AppItem>? {
        return packageManager.getInstalledPackages(0)
            .filter { it.applicationInfo != null && it.appProvisionType == provisionType }
            .map {
                AppItem(
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

    override fun onPostExecute(result: List<AppItem>) {
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