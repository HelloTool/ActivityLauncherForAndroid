@file:Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")

package io.gitee.jesse205.activitylauncher.features.activitylist

import android.app.Application
import android.content.pm.PackageManager
import android.os.AsyncTask
import io.gitee.jesse205.activitylauncher.model.LoadedActivityInfo

class LoadActivitiesTask(
    private val application: Application,
    private val packageName: String,
    private val onBeforeLoad: () -> Unit,
    private val onLoad: (List<LoadedActivityInfo>) -> Unit,
    private val onCancel: () -> Unit,
) : AsyncTask<Void?, Void?, List<LoadedActivityInfo>>() {
    private var isTaskIgnored = false

    override fun doInBackground(vararg voids: Void?): List<LoadedActivityInfo> {
        return runCatching {
            application.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES).activities
        }.getOrNull()
            ?.map {
                LoadedActivityInfo(activityInfo = it)
            }
            ?.sortedWith { a, b ->
                if (a.activityInfo.exported != b.activityInfo.exported) {
                    return@sortedWith if (a.activityInfo.exported) -1 else 1
                }
                a.name.compareTo(b.name, true)
            } ?: listOf()
    }

    override fun onPreExecute() {
        if (!isTaskIgnored) {
            onBeforeLoad()
        }
    }

    override fun onPostExecute(result: List<LoadedActivityInfo>) {
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
}
