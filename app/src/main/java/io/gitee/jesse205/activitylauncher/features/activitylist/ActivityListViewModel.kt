package io.gitee.jesse205.activitylauncher.features.activitylist

import android.app.Application
import android.os.Bundle
import android.os.Parcelable
import io.gitee.jesse205.activitylauncher.app.BaseViewModel
import io.gitee.jesse205.activitylauncher.utils.getParcelableCompat
import kotlinx.parcelize.Parcelize


class ActivityListViewModel(
    packageName: String?
) : BaseViewModel<ActivityListViewModel.ActivityListActivityStateListener>() {

    var loadActivitiesTask: LoadActivitiesTask? = null

    var packageName: String = packageName ?: ""
        private set(value) {
            field = value
            listeners.forEach { it.onPackageNameUpdate(value) }
        }

    var activities: List<AppActivityItem>? = null
        private set(value) {
            field = value
            listeners.forEach { it.onActivitiesUpdate(value) }
        }

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

    override fun saveHierarchyState() = Bundle().apply {
        putParcelable(SAVED_STATE_TAG, SavedState(packageName))
    }

    override fun restoreHierarchyState(state: Bundle?) {
        state?.getParcelableCompat(SAVED_STATE_TAG, SavedState::class.java)?.let {
            packageName = it.packageName
        }
    }

    @Parcelize
    data class SavedState(
        val packageName: String
    ) : Parcelable

    interface ActivityListActivityStateListener {
        fun onActivitiesUpdate(activities: List<AppActivityItem>?)
        fun onActivitiesLoadingUpdate(isActivitiesLoading: Boolean)
        fun onPackageNameUpdate(packageName: String)
    }

    companion object {
        const val SAVED_STATE_TAG = "savedState"
    }
}
