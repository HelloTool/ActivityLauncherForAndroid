package io.gitee.jesse205.activitylauncher.features.applist

import android.os.Parcelable
import io.gitee.jesse205.activitylauncher.model.LoadedAppInfo
import io.gitee.jesse205.activitylauncher.utils.AppProvisionType
import io.gitee.jesse205.activitylauncher.utils.AppSortCategory
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize


@Parcelize
class MainActivityState(
    var sortCategory: AppSortCategory = AppSortCategory.INSTALL_TIME,
    var provisionType: AppProvisionType = AppProvisionType.USER
) : Parcelable {
    @IgnoredOnParcel
    var apps: List<LoadedAppInfo>? = null

    @IgnoredOnParcel
    var loadAppsTask: LoadAppsTask? = null

    @IgnoredOnParcel
    var isAppsLoading = false

    val isAppsLoadingOrLoaded get() = isAppsLoading || apps != null
}