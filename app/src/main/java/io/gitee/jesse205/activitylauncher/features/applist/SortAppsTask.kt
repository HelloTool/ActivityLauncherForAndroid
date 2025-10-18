@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package io.gitee.jesse205.activitylauncher.features.applist

import android.app.Application
import android.content.pm.PackageManager
import android.os.AsyncTask
import io.gitee.jesse205.activitylauncher.utils.AppSortCategory

class SortAppsTask(
    application: Application,
    private val apps: List<AppModel>,
    private val sortCategory: AppSortCategory,
    private val onBeforeSort: () -> Unit,
    private val onSort: (List<AppModel>) -> Unit,
    private val onCancel: () -> Unit,
) : AsyncTask<Void, Void, List<AppModel>>() {
    private val packageManager: PackageManager = application.packageManager
    private var isTaskIgnored = false

    override fun doInBackground(vararg params: Void): List<AppModel>? {
        return when (sortCategory) {
            AppSortCategory.NAME -> apps.sortedWith { a, b ->
                a.getOrLoadLabel(packageManager).toString()
                    .compareTo(b.getOrLoadLabel(packageManager).toString(), true)
            }

            AppSortCategory.INSTALL_TIME -> apps.sortedByDescending { it.firstInstallTime }
            AppSortCategory.UPDATE_TIME -> apps.sortedByDescending { it.lastUpdateTime }
        }
    }

    override fun onPreExecute() {
        if (!isTaskIgnored) {
            onBeforeSort()
        }
    }

    override fun onPostExecute(result: List<AppModel>) {
        if (!isTaskIgnored) {
            onSort(result)
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
        const val TAG = "SortAppsTask"
    }
}
