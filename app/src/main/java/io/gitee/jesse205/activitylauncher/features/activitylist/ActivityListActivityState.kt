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
    var isActivitiesLoading = false

    val isActivitiesLoadingOrLoaded get() = isActivitiesLoading || activities != null
}
