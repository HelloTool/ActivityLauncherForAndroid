@file:Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")

package io.gitee.jesse205.activitylauncher.features.activitylist

import android.app.Application
import android.content.pm.PackageManager
import android.os.AsyncTask

class LoadActivitiesTask(
    private val application: Application,
    private val packageName: String,
    private val onBeforeLoad: () -> Unit,
    private val onLoad: (List<AppActivityItem>) -> Unit,
    private val onCancel: () -> Unit,
) : AsyncTask<Void?, Void?, List<AppActivityItem>>() {
    private var isTaskIgnored = false

    override fun doInBackground(vararg voids: Void?): List<AppActivityItem> {
        val activities = runCatching {
            application.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES).activities
        }.getOrNull() ?: return emptyList()

        return activities.map {
            AppActivityItem(activityInfo = it)
        }.sortedWith { a, b ->
            if (a.activityInfo.exported != b.activityInfo.exported) {
                return@sortedWith if (a.activityInfo.exported) -1 else 1
            }
            a.name.compareTo(b.name, true)
        }
    }

    override fun onPreExecute() {
        if (!isTaskIgnored) {
            onBeforeLoad()
        }
    }

    override fun onPostExecute(result: List<AppActivityItem>) {
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
