package io.gitee.jesse205.activitylauncher.features.activitylist

import android.app.Application
import android.os.Parcelable
import io.gitee.jesse205.activitylauncher.app.BaseActivityState
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
class ActivityListActivityState(
    private var _packageName: String
) : BaseActivityState<ActivityListActivityState.ActivityListActivityStateListener>(), Parcelable {

    @IgnoredOnParcel
    var loadActivitiesTask: LoadActivitiesTask? = null

    var packageName: String
        get() = _packageName
        private set(value) {
            _packageName = value
            listeners.forEach { it.onPackageNameUpdate(value) }
        }

    @IgnoredOnParcel
    var activities: List<AppActivityModel>? = null
        private set(value) {
            field = value
            listeners.forEach { it.onActivitiesUpdate(value) }
        }

    @IgnoredOnParcel
    var isActivitiesLoading = false
        private set(value) {
            field = value
            listeners.forEach { it.onActivitiesLoadingUpdate(value) }
        }

    val isActivitiesLoadingOrLoaded get() = isActivitiesLoading || activities != null

    fun loadActivities(application: Application) {
        loadActivitiesTask?.apply {
            ignore()
            @Suppress("DEPRECATION")
            cancel(true)
        }
        loadActivitiesTask = LoadActivitiesTask(
            application = application,
            packageName = packageName,
            onBeforeLoad = {
                isActivitiesLoading = true
                activities = null

            },
            onLoad = {
                isActivitiesLoading = false
                activities = it
            },
            onCancel = {
                isActivitiesLoading = false
            }
        ).apply {
            @Suppress("DEPRECATION")
            execute()
        }
    }

    override fun destroy() {
        super.destroy()
        loadActivitiesTask?.apply {
            ignore()
            @Suppress("DEPRECATION")
            cancel(true)
        }
    }

    interface ActivityListActivityStateListener {
        fun onActivitiesUpdate(activities: List<AppActivityModel>?)
        fun onActivitiesLoadingUpdate(isActivitiesLoading: Boolean)
        fun onPackageNameUpdate(packageName: String)
    }
}
