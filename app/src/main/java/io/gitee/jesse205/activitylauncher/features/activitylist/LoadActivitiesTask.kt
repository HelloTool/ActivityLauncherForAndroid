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
        } ?: listOf()
    }

    override fun onPreExecute() {
        weakActivity?.apply {
            setLoadingActivities(true)
            setActivities(listOf())
        }
        state.apply {
            isLoadingActivities = true
            activities = listOf()
            loadActivitiesTask = this@LoadActivitiesTask
        }
    }

    override fun onPostExecute(result: List<LoadedActivityInfo>) {
        if (!isCurrentTaskRunning) return
        weakActivity?.apply {
            setLoadingActivities(false)
            setActivities(result)
        }
        state.apply {
            isLoadingActivities = false
            activities = result
            loadActivitiesTask = null
        }
    }

    override fun onCancelled() {
        if (!isCurrentTaskRunning) return
        weakActivity?.takeIf { !it.isFinishing }?.apply {
            setLoadingActivities(false)
        }

        state.apply {
            isLoadingActivities = false
            loadActivitiesTask = null
        }
    }
}
