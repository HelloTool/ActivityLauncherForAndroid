package io.gitee.jesse205.activitylauncher.features.activitylist

import android.app.Application
import android.content.pm.PackageManager
import io.gitee.jesse205.activitylauncher.core.BaseActivityAsyncTask
import io.gitee.jesse205.activitylauncher.model.LoadedActivityInfo

@Suppress("OVERRIDE_DEPRECATION")
class LoadActivitiesTask(
    val application: Application,
    activity: ActivityListActivity,
    state: ActivityListActivityState
) :
    BaseActivityAsyncTask<ActivityListActivity, ActivityListActivityState, Void?, Void?, List<LoadedActivityInfo>>(
        activity,
        state
    ) {
    val isCurrentTaskRunning get() = state.loadActivitiesTask == this
    override fun doInBackground(vararg voids: Void?): List<LoadedActivityInfo> {
        val packageInfoResult = runCatching {
            application.packageManager.getPackageInfo(state.packageName!!, PackageManager.GET_ACTIVITIES)
        }
        return packageInfoResult.getOrNull()?.activities?.map {
            LoadedActivityInfo(activityInfo = it)
        }?.sortedWith { a, b ->
            if (a.activityInfo.exported != b.activityInfo.exported) {
                return@sortedWith if (a.activityInfo.exported) -1 else 1
            }
            a.name.compareTo(b.name, true)
        } ?: listOf()
    }

    override fun onPreExecute() {
        weakActivity?.apply {
            state.apply {
                isActivitiesLoading = true
                activities = listOf()
                loadActivitiesTask = this@LoadActivitiesTask
            }
            refreshActivitiesLoading()
            refreshActivities()
        }
    }

    override fun onPostExecute(result: List<LoadedActivityInfo>) {
        if (!isCurrentTaskRunning) return
        state.apply {
            isActivitiesLoading = false
            activities = result
            loadActivitiesTask = null
        }
        weakActivity?.apply {
            refreshActivitiesLoading()
            refreshActivities()
        }
    }

    override fun onCancelled() {
        if (!isCurrentTaskRunning) return
        state.apply {
            isActivitiesLoading = false
            loadActivitiesTask = null
        }
        weakActivity?.takeIf { !it.isFinishing }?.apply {
            refreshActivitiesLoading()
        }

    }
}
