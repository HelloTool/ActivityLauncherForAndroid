package io.gitee.jesse205.activitylauncher.features.activitylist

import android.os.AsyncTask
import android.os.Parcelable
import io.gitee.jesse205.activitylauncher.model.LoadedActivityInfo
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
class ActivityListActivityState(
    var packageName: String? = null
) : Parcelable {
    @IgnoredOnParcel
    var activities: List<LoadedActivityInfo>? = null

    @IgnoredOnParcel
    var loadActivitiesTask: LoadActivitiesTask? = null

    @IgnoredOnParcel
    @Suppress("DEPRECATION")
    val isLoadingActivitiesTaskRunning get() = loadActivitiesTask?.status == AsyncTask.Status.RUNNING

    @IgnoredOnParcel
    var isLoadingActivities = false
}
